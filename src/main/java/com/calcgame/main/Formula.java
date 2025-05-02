package com.calcgame.main;

import com.calcgame.main.buttons.CalculateButton;
import org.jetbrains.annotations.Nullable;
import org.python.core.PyComplex;

import java.util.Map;
import java.util.function.Function;

public class Formula {
    protected Formula a;
    protected @Nullable Formula b;
    protected @Nullable Operation op;
    protected @Nullable Function<PyComplex, PyComplex> f;
    protected @Nullable PyComplex constant;
    protected @Nullable String variable;
    private int tmp_return_info = -1;

    protected Formula() {
    }

    public Formula(Formula a, @Nullable Operation op, @Nullable Formula b) {
        this.a = a;
        this.op = op;
        this.b = b;
    }

    public Formula(@Nullable PyComplex constant) {
        this.constant = constant;
    }

    public Formula(@Nullable String variable) {
        this.variable = variable;
    }

    public PyComplex calc() throws InvalidFormulaException {
        return calc(Map.of());
    }

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

    public int countOperations() throws InvalidFormulaException {
        if (constant != null) return 0;
        else if (variable != null) return 0;
        else if (op != null && b != null) return a.countOperations() + 1 + b.countOperations();
        else if (f != null) return 1 + a.countOperations();
        else
            throw new InvalidFormulaException("Formula does not contain an operation and second operand or unary function");
    }

    public String toString(GameState state) throws InvalidFormulaException {
        if (variable != null) return variable;
        if (constant != null) return state.numToString(constant);
        if (f != null) return CalculateButton.rev_funcs.get(f) + '(' + a.toString(state) + ')';
        if (op == null || b == null)
            throw new InvalidFormulaException("Formula does not contain an operation and second operand");
        return '(' + a.toString(state) + CalculateButton.rev_ops.get(op) + b.toString(state) + ')';
    }

    public Formula andThen(Function<PyComplex, PyComplex> f) {
        Formula formula = new Formula();
        formula.a = this;
        formula.f = f;
        return formula;
    }

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

    public static Formula fromString(String s) throws InvalidFormulaException {
        String tmp = s.replaceAll("(?<=[0-9])(eE)(?=[0-9])", "*10^");
        return fromString(tmp, 0, tmp.length(), 0);
    }

    public static class InvalidFormulaException extends Exception {
        public InvalidFormulaException(String s) {
            super(s);
        }
    }
}
