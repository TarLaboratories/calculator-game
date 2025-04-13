if state.chance(1, 16): # wheel of fortune 1 in 4 chance
    for i in state.getCurrentButtons().getButtons():
        if i != state.getButton('WOF'):
            state.getCurrentButtons().addCount(i, complex(3))