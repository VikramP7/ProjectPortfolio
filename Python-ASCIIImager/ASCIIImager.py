import math
import numpy as np
from PIL import Image

# window sizes for resultant image in characters
width = 240
height = 135
charDencity = [chr(209), chr(64), chr(35), chr(87), chr(36), chr(57), chr(56), chr(55), chr(54), chr(53), chr(52),
               chr(51), chr(50), chr(49), chr(48), chr(63), chr(33), chr(97), chr(98), chr(99), chr(59), chr(58),
               chr(43), chr(61), chr(45), chr(44), chr(46), chr(95)]


def ColourToAscii(r, g, b):
    setForeground = u"\u001b[38;2;"
    retString = setForeground + str(r) + ";" + str(g) + ";" + str(b) + "m"
    return retString


def ColourToBrightness(r, g, b):
    ave = (r+g+b)/3.0
    dencityPercent = ave/255.0
    charIndex = round(dencityPercent*len(charDencity))
    char = charDencity[charIndex-1]
    return char


def GetPath():
    pooImagePath = True
    path = ""
    while pooImagePath:
        pooImagePath = False
        path = input("What is the path for your image (include extension): ")
        try:
            image = Image.open(path)
        except TypeError:
            pooImagePath = True
            print("File not parsable.")
        except FileNotFoundError:
            pooImagePath = True
            print("No file found at path.")
    return path



def PrintImage(path):
    #print(len(imgArr))
    #print(len(imgArr[0]))

    image = Image.open(path)
    imgArr = np.asarray(image)

    # set result image
    scaleFactor = round(len(imgArr) / 70)
    resizeWidth = int(len(imgArr) / scaleFactor)
    resizeHeight = int(len(imgArr[0]) / scaleFactor)
    resizeImg = []

    for x in range(0, resizeWidth):
        resizeImg.append([])  # row
        for y in range(0, resizeHeight):
            resizeImg[x].append([])  # another array for r green and blue
            for rgb in range(0, 3):
                resizeImg[x][y].append(0)

    # find the average colour of the area that is scaled down
    for x in range(0, len(resizeImg)):
        for y in range(0, len(resizeImg[x])):
            rAve = 0
            gAve = 0
            bAve = 0
            for i in range(0, scaleFactor):
                for j in range(0, scaleFactor):
                    rAve += imgArr[(x * scaleFactor) + i][(y * scaleFactor) + j][0]
                    gAve += imgArr[(x * scaleFactor) + i][(y * scaleFactor) + j][1]
                    bAve += imgArr[(x * scaleFactor) + i][(y * scaleFactor) + j][2]
            rAve = rAve / (scaleFactor * scaleFactor)
            gAve = gAve / (scaleFactor * scaleFactor)
            bAve = bAve / (scaleFactor *   scaleFactor)
            resizeImg[x][y][0] = int(rAve)
            resizeImg[x][y][1] = int(gAve)
            resizeImg[x][y][2] = int(bAve)

    line = ""
    lines = []
    for x in range(0, len(resizeImg)):
        for y in range(0, len(resizeImg[x])):
            # doing it twice as text is taller than wider
            line += ColourToAscii(resizeImg[x][y][0], resizeImg[x][y][1], resizeImg[x][y][2])
            line += ColourToBrightness(resizeImg[x][y][0], resizeImg[x][y][1], resizeImg[x][y][2])
            line += ColourToAscii(resizeImg[x][y][0], resizeImg[x][y][1], resizeImg[x][y][2])
            line += ColourToBrightness(resizeImg[x][y][0], resizeImg[x][y][1], resizeImg[x][y][2])
        lines.append(u"" + line)
        line = ""

    print(u"\u001b[0m")
    return lines


if __name__ == "__main__":
    outString = PrintImage(GetPath())
    for i in range(0, len(outString)):
        print(outString[i])
    print(u"\u001b[0m")

