def lol(ctx):
    state = ctx.state()
    buttons = state.getCurrentButtons()
    this_button = state.getButton('base:>:)')
    this_coords = ctx.pos()
    target = state.randomChoice([i for i in buttons.getNeighbourCoords(this_coords) if not buttons.getButton(i).getString() == '='])
    buttons.setButton(this_coords, state.getButton('base:X'))
    buttons.setButton(target, this_button)
    ctx.pos().x = target.x
    ctx.pos().y = target.y

def on_round_start(ctx):
    print("on_round_start")
    lol(ctx)
    state = ctx.state()
    state.setMoney(state.getMoney() + 4)

def on_click(ctx):
    print("on_click")
    state = ctx.state()
    if not state.chance(1, 2):
        lol(ctx)
    else:
        state.getCurrentButtons().setButton(state.getCurrentButtons().getCoords(state.getButton('base:>:)')), state.getRandomButton())
        state.removeOnRoundStart("walker-button")

def on_add(ctx):
    print("on_add")
    ctx.state().onRoundStart("walker-button", lambda: on_round_start(ctx))

print("idk")