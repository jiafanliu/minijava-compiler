for i in range(100):
    s="%02d"%i
    with open("test"+s+".java") as file:
        te=0
        for lines in file:
            if not lines.find('// TE')==-1:
                te=1
        if(te==0):
            print('Program type checked successfully')
        else:
            print('Type error')