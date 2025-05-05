package com.calcgame.main;

import com.calcgame.main.buttons.CalculateButton;
import org.jetbrains.annotations.Nullable;
import org.python.core.PyComplex;

import java.util.Map;
import java.util.function.Function;

/**
 * An object that represents a mathematical expression
 */
public class Formula {
    /**
     * The first operand if this object represents an operation,
     * the argument if this object represents a function, {@code null} otherwise
     */
    protected Formula a;

    /**
     * The second operand if this object represents an operation, {@code null} otherwise
     */
    protected @Nullable Formula b;

    /**
     * The operation this object represents, or {@code null} if it doesn't represent an operation
     */
    protected @Nullable Operation op;

    /**
     * The function this object represents, or {@code null} if it doesn't represent a function
     */
    protected @Nullable Function<PyComplex, PyComplex> f;

    /**
     * The value this object represents, or {@code null} if it doesn't represent a value
     */
    protected @Nullable PyComplex constant;

    /**
     * The name of the variable this object represents, or {@code null} if it doesn't represent a variable
     */
    protected @Nullable String variable;

    /**
     * Some weird thing that breaks everything if removed.
     * Probably used when parsing a formula
     */
    private int tmp_return_info = -1;

    /**
     * Leaves all fields {@code null}
     */
    protected Formula() {
    }

    /**
     * Constructs a formula with the specified operation
     * @param a the first operand
     * @param op the operation
     * @param b the second operand
     */
    public Formula(Formula a, @Nullable Operation op, @Nullable Formula b) {
        this.a = a;
        this.op = op;
        this.b = b;
    }

    /**
     * Constructs a formula with the specified constant
     * @param constant the number this formula will represent
     */
    public Formula(@Nullable PyComplex constant) {
        this.constant = constant;
    }

    /**
     * Constructs a formula with the specified string as it's variable name
     * @param variable the variable name
     */
    public Formula(@Nullable String variable) {
        this.variable = variable;
    }

    /**
     * Evaluates this formula
     * @return the result of evaluating this formula
     * @throws InvalidFormulaException if this formula is invalid or contains a variable
     */
    public PyComplex calc() throws InvalidFormulaException {
        return calc(Map.of());
    }

    /**
     * Evaluates this formula
     * @param vars the variables to use
     * @return the result of evaluating this formula
     * @throws InvalidFormulaException if this formula is invalid or a variable is undefined
     */
    public PyComplex calc(Map<String, PyComplex> vars) throws InvalidFormulaException {
        if (variable != null) {
            if (vars.containsKey(variable)) return vars.get(variable);
            else
                throw new InvalidFormulaException("Formula contains a reference to variable '%s', which is not defined".formatted(variable));
        } else if (constant != null) return constant;
        else if (op != null && b != null) return op.f().apply(a.calc(vars), b.calc(vars));
        else if (f != null) return f.apply(a.calc(vars));
        else
            throw new InvalidFormulaException("Formula does not contain an operation and second operand or unary function");
    }

    /**
     * Returns the amount of operations in this formula
     * @return the amount of operations
     * @throws InvalidFormulaException if this formula is invalid
     */
    public int countOperations() throws InvalidFormulaException {
        if (constant != null) return 0;
        else if (variable != null) return 0;
        else if (op != null && b != null) return a.countOperations() + 1 + b.countOperations();
        else if (f != null) return 1 + a.countOperations();
        else
            throw new InvalidFormulaException("Formula does not contain an operation and second operand or unary function");
    }

    /**
     * Returns a human-readable string representation of this object
     * @param state the state to use to stringify numbers using {@link GameState#numToString(PyComplex)}, if {@code null} uses {@link PyComplex#toString()}
     * @return a human-readable string representation of this object
     * @throws InvalidFormulaException if this formula is invalid
     */
    public String toString(@Nullable GameState state) throws InvalidFormulaException {
        if (variable != null) return variable;
        if (constant != null) return state == null ? constant.toString() : state.numToString(constant);
        if (f != null) return CalculateButton.rev_funcs.get(f) + '(' + a.toString(state) + ')';
        if (op == null || b == null)
            throw new InvalidFormulaException("Formula does not contain an operation and second operand");
        return '(' + a.toString(state) + CalculateButton.rev_ops.get(op) + b.toString(state) + ')';
    }

    /**
     * Returns a human-readable string representation of this object.
     * Equivalent to using {@code toString(null)}.
     * @return the human-readable string
     * @throws RuntimeException if this formula is invalid
     */
    public String toString() {
        try {
            return toString(null);
        } catch (InvalidFormulaException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wraps this formula in a function and returns the resulting formula.
     * Returns {@code f(x)}, where this formula is {@code x}, and the function is {@code f}.
     * @param f the function to wrap into
     * @return the resulting formula
     */
    public Formula andThen(Function<PyComplex, PyComplex> f) {
        Formula formula = new Formula();
        formula.a = this;
        formula.f = f;
        return formula;
    }

    /**
     * Parses a mathematical expression from a string
     * @param s the string to parse from
     * @param start the start index
     * @param end the end index
     * @param prev_priority the priority of the previous operation if there was any (used when calling recursively), {@code 0} otherwise
     * @return the parsed formula
     * @throws InvalidFormulaException if the string is not a valid mathematical expression
     */
    private static Formula fromString(String s, int start, int end, int prev_priority) throws InvalidFormulaException {
        Formula cur = new Formula();
        cur.constant = new PyComplex(0);
        StringBuilder cur_func = new StringBuilder();
        int last_op = start - 1;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c == 'j') {
                if (cur.constant == null) {
                    if (cur.op == null)
                        cur.constant = c == 'j' ? new PyComplex(0, 1) : new PyComplex(Integer.parseInt(Character.toString(c)));
                    else {
                        cur.b = fromString(s, i, end, cur.op.priority());
                        if (cur.b.tmp_return_info != -1)
                            i = cur.b.tmp_return_info;
                    }
                } else {
                    cur.constant = c == 'j' ? cur.constant.__mul__(new PyComplex(0, 1)).__complex__() : cur.constant.__mul__(new PyComplex(10)).__add__(new PyComplex(Integer.parseInt(Character.toString(c)))).__complex__();
                }
            } else if (Character.isLetter(c)) {
                cur_func.append(c);
            } else if (c == '(') {
                int tmp = i;
                while (i < end && s.charAt(i) != ')') i++;
                if (s.charAt(i) != ')') throw new InvalidFormulaException("Unclosed bracket at " + tmp);
                Formula idk = fromString(s, tmp + 1, i, 0);
                if (!cur_func.isEmpty()) {
                    if (!CalculateButton.funcs.containsKey(cur_func.toString()))
                        throw new InvalidFormulaException("Function does not exist: " + cur_func);
                    idk = idk.andThen(CalculateButton.funcs.get(cur_func.toString()));
                    cur_func = new StringBuilder();
                }
                idk.tmp_return_info = -2;
                if (cur.constant == null) {
                    if (cur.op == null) cur = idk;
                    else cur.b = idk;
                } else cur = idk;
                last_op = tmp - 1;
            } else if (c == ')')
                throw new InvalidFormulaException("Unexpected closing bracket at " + i);
            else {
                if (!cur_func.isEmpty()) {
                    cur.variable = cur_func.toString();
                    cur_func = new StringBuilder();
                }
                if (!CalculateButton.ops.containsKey(Character.toString(c)))
                    throw new InvalidFormulaException("Operation does not exist: " + c);
                Operation op = CalculateButton.ops.get(Character.toString(c));
                if (op.priority() <= prev_priority) {
                    cur.tmp_return_info = i - 1;
                    return cur;
                }
                if (cur.op == null) {
                    cur.a = new Formula();
                    cur.a.constant = cur.constant;
                    cur.a.variable = cur.variable;
                    cur.constant = null;
                    cur.variable = null;
                    cur.op = op;
                    last_op = i;
                } else {
                    if (cur.op.priority() >= op.priority() || cur.tmp_return_info == -2) {
                        if (cur.a != null && cur.b == null && c == '-') {
                            cur = new Formula(cur.a, cur.op, new Formula(new Formula(new PyComplex(0.)), op, fromString(s, i + 1, end, cur.op.priority())));
                            assert cur.b != null && cur.b.b != null;
                            if (cur.b.b.tmp_return_info != -1)
                                i = cur.b.b.tmp_return_info;
                        } else {
                            cur = new Formula(cur, op, null);
                            last_op = i;
                        }
                    } else {
                        cur = new Formula(cur.a, cur.op, fromString(s, last_op + 1, end, cur.op.priority()));
                        assert cur.b != null;
                        if (cur.b.tmp_return_info != -1)
                            i = cur.b.tmp_return_info;
                    }
                }
            }
        }
        cur.tmp_return_info = end;
        if (!cur_func.isEmpty() && cur.op != null) cur.b = new Formula(cur_func.toString());
        else if (!cur_func.isEmpty()) cur.variable = cur_func.toString();
        return cur;
    }

    /**
     * Parses a mathematical expression from a string without whitespace
     * @param s the string to parse from
     * @return the parsed formula
     * @throws InvalidFormulaException if the string is not a valid mathematical expression
     */
    public static Formula fromString(String s) throws InvalidFormulaException {
        String tmp = s.replaceAll("(?<=[0-9])(eE)(?=[0-9])", "*10^");
        return fromString(tmp, 0, tmp.length(), 0);
    }

    /**
     * Thrown if the formula provided is invalid
     */
    public static class InvalidFormulaException extends Exception {
        /**
         * Constructs the exception
         * @param s the message to log when caught
         */
        public InvalidFormulaException(String s) {
            super(s);
        }
    }
}
