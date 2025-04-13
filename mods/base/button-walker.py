def lol():
    buttons = state.getCurrentButtons()
    this_button = state.getButton('>:)')
    this_coords = buttons.getCoords(this_button)
    target = state.randomChoice([i for i in buttons.getNeighbourCoords(this_button) if not buttons.getButton(i).getString() == '='])
    buttons.setButton(this_coords, state.getButton('X'))
    buttons.setButton(target, this_button)

def on_round_start():
    lol()
    state.setMoney(state.getMoney() + 4)

state.onRoundStart("walker-button", on_round_start)
if not state.chance(1, 4):
    lol()
else:
    state.getCurrentButtons().setButton(state.getCurrentButtons().getCoords(state.getButton('>:)')), state.getRandomButton())
    state.removeOnRoundStart("walker-button")