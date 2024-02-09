import sys

def main():
    totalNumberOfQueries = 0
    tsTimeTotal = 0
    tjTimeTotal = 0

    for i in range(1, len(sys.argv)):
        logfile = open(sys.argv[i], 'r')
        while True:
            timeRow = logfile.readline()
            if not timeRow:
                break
            timeDataList = timeRow.split(',')
            tsTimeTotal += int(timeDataList[0])
            tjTimeTotal += int(timeDataList[1])
            totalNumberOfQueries += 1
            
    print(f"TS Average Time: {round(round(tsTimeTotal/totalNumberOfQueries,4)/1000000,4)}ms")
    print(f"TJ Average Time: {round(round(tjTimeTotal/totalNumberOfQueries,4)/1000000,4)}ms")



if (__name__ == "__main__"):
    main()
