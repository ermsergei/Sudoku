
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Sudoku {

    static int size = 3;

    public static void main(String[] args) throws IOException {
        System.out.println("Start read InPut file!");

        Matrix matrix = new Matrix();
        FileReader fin;
        try {
            fin = new FileReader(args[0]);
        } catch (FileNotFoundException e) {
            System.out.println("File is not found");
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Using: ShowFile");
            return;
        }

        Scanner src = new Scanner(fin);

        int line = 0;
        int column = -1;
        int val = 0;

        while (src.hasNext()) {
            if (src.hasNextInt()) {
                val = src.nextInt();
                column++;
                if (column >= size * size) {
                    //   System.out.println();
                    line++;
                    column = 0;
                }
                // System.out.println(val + "[" + line + "][" + column + "] ");
                if (val > 0) {
                    matrix.SetValue(line, column, val);
                }
            }
        }

        fin.close();
        // matrix.Display("Matrix before solve", 1);
        if (matrix.SolveAll(0) == 0) {
            System.out.println("Success!!");
            matrix.Display("Solution:", 1);
            matrix.CheckFinalMatrix();
        } else {
            System.out.println("There is no solution for this Matrix");
        }

    }
}

class Cell {

    int size = Sudoku.size;
    int value;
    int NumberOfPossVal;
    int possibleValues[];

    Cell() {
        value = 0;  //init of value in the Cell using an object cells
        NumberOfPossVal = size * size;
        possibleValues = new int[size * size];
        for (int ii = 0; ii < size * size; ii++) {
            possibleValues[ii] = ii + 1;
        }
    }
}

class Matrix {

    int size = Sudoku.size;
    Cell[][] cells = new Cell[size * size][size * size];

    Matrix() {
        InitMatrix();
    }

    Matrix(Matrix from) {
        InitMatrix(); 
        copyMatrix(from);
    }
    private int copyMatrix(Matrix workingCopy) {
        int line, column, ii;

        for (line = 0; line < size * size; ++line) {
            for (column = 0; column < size * size; ++column) {
                if (workingCopy.cells[line][column].NumberOfPossVal > 0) {
                    for (ii = 0; ii < size * size; ++ii) {
                        cells[line][column].possibleValues[ii] = workingCopy.cells[line][column].possibleValues[ii];
                    }
                }
                cells[line][column].value = workingCopy.cells[line][column].value;
                cells[line][column].NumberOfPossVal = workingCopy.cells[line][column].NumberOfPossVal;
            }
        }
        return 0;
    }

    private void InitMatrix() {
        int line, column;
        for (line = 0; line < size * size; ++line) {
            for (column = 0; column < size * size; ++column) {
                cells[line][column] = new Cell();         //Making new Cell to reset the Cell of the Matrix
            }
        }
    }

    int SetValue(int line, int column, int value) {
        // If the value is already set - error
        cells[line][column].value = value;
        //System.out.println("Value: "+ value);
        //Update possible values for the row, column and quadrant
        if (UpdatePosValues(line, column, value) < 0) {
            return -1;
        }
        cells[line][column].NumberOfPossVal = 0;
        return 0;

    }

    //Function Updates possible values for the row, column and quadrant
    int UpdatePosValues(int line, int column, int value) {
        int ii, jj;
        // Update for the line
        for (ii = 0; ii < size * size; ++ii) {
            if (UpdatePossValuesForCell(line, ii, value) < 0) {
                return -1;
            }
        }

        // For the column
        for (jj = 0; jj < size * size; ++jj) {
            if (UpdatePossValuesForCell(jj, column, value) < 0) {
                return -1;
            }
        }

        // For the quadrant
        int top;
        int left;

        top = line / size;
        left = column / size;

        for (ii = 0; ii < size; ++ii) {
            for (jj = 0; jj < size; ++jj) {
                if (UpdatePossValuesForCell(top * size + ii, left * size + jj, value) < 0) {
                    return -1;
                }
            }
        }
        return 0;
    }
    // Function updates possible values of the Cell

    int UpdatePossValuesForCell(int line, int column, int value) {

        if (cells[line][column].possibleValues[value - 1] != 0) {
            cells[line][column].NumberOfPossVal--;
            cells[line][column].possibleValues[value - 1] = 0;


            // If we remove the last possible value for the cell - it's an error.
            if (cells[line][column].NumberOfPossVal == 0 && cells[line][column].value == 0) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }

    //Scanning matrix by lines & columns
    //Function is searching cells which have only one NumberOfPosVal, then calls SetValue() &
    //set this value
    int Solve() {
        System.out.println();
        System.out.print(" Solve starts");
        int ret = 0;
        int ii, jj;
        System.out.println();

        for (ii = 0; ii < size * size; ++ii) {
            for (jj = 0; jj < size * size; ++jj) {
                if (cells[ii][jj].value != 0) // System.out.println("Value " + cells[ii][jj].value);
                {
                    continue;
                }
                //  System.out.println("NumberOfPossVal [" + ii + "][" + jj + "]" + cells[ii][jj].NumberOfPossVal + " Value " + cells[ii][jj].value);
                if (cells[ii][jj].NumberOfPossVal == 1) {
                    // System.out.println("Cell has only one possible value - resolve " + ii + jj);
                    int kk;
                    for (kk = 0; kk < size * size; ++kk) {
                        if (cells[ii][jj].possibleValues[kk] != 0) {
                            if (SetValue(ii, jj, kk + 1) < 0) {
                                return -1;
                            }
                            ret++;
                            break;
                        }
                    }
                }
            }
        }
        if (ret == 0) {
            DisplayPoss("After Solve");
        }
        System.out.println(" Solve returns " + ret);
        return ret;
    }
    /*
     * Solve1()is looking for possibleValues, which appears in the line only
     * once, if sach value exists other possibleValues of the cell will be
     * removed and after SetValue() will set this value into the cell
     */

    int Solve1() {
        System.out.println();
        System.out.print(" Solve1 starts");
        int ret = 0;
        int line;
        int column;

        // Scan lines
        for (line = 0; line < size * size; line++) {
            int value;
            for (value = 1; value <= size * size; ++value) {
                int whatColumn = -1;

                for (column = 0; column < size * size; ++column) {
                    if (cells[line][column].value != 0) {
                        continue;
                    }
                    if (cells[line][column].possibleValues[value - 1] != 0) {
                        if (whatColumn == -1) {
                            whatColumn = column;
                        } else {
                            // Value appears the second time - skip it
                            whatColumn = -1;
                            break; // out from loop
                        }
                    }
                }
                if (whatColumn == -1) {
                    // Nothing to do
                } else {
                    // Value appears only in cell [line][column]
                    // printf("Value %d appears only in cell [%d][%d], set it\n",
                    //	   value, line, whatColumn);

                    if (SetValue(line, whatColumn, value) < 0) {
                        return -1;
                    }
                    ret++;
                }
            }
        }
        if (ret == 0) {
            DisplayPoss("After Solve1");
        }
        System.out.println(" Solve1 returns " + ret);
        return ret;
    }
    /*
     * Solve2() is looking for possibleValues, which appears in the column only
     * once, if sach value exists other possibleValues of the cell will be
     * removed and after SetValue() will set this value into the cell
     */

    int Solve2() {
        System.out.println("Solve2 starts");
        int ret = 0;
        int line, column;
        for (column = 0; column < size * size; column++) {
            int value;
            for (value = 1; value <= size * size; ++value) {
                int whatLine = -1;

                for (line = 0; line < size * size; ++line) {
                    if (cells[line][column].value != 0) {
                        continue;
                    }

                    if (cells[line][column].possibleValues[value - 1] != 0) {
                        if (whatLine == -1) {
                            whatLine = line;
                        } else {
                            // Value appears the second time - skip it
                            whatLine = -1;
                            break; // out from loop
                        }
                    }
                }
                if (whatLine == -1) {
                    // Nothing to do
                } else {
                    // Value appears only in cell [line][column]
                    //printf("Value %d appears only in cell [%d][%d], set it\n",
                    //   value, whatLine, column);
                    if (SetValue(whatLine, column, value) < 0) {
                        return -1;
                    }
                    ret++;
                }
            }
        }
        if (ret != 0) {
            DisplayPoss("After Solve2");
        }
        System.out.println();
        System.out.println(" Solve2 returns " + ret);
        return ret;
    }

    /*
     * Solve3() is looking for possibleValues, which appears in the quadrant
     * only once, if sach value exists other possibleValues of the cell will be
     * removed and after SetValue() will set this value into the cell
     */
    int Solve3() {
        System.out.println("Solve3 starts");
        int ret = 0;
        int li, ci;
        for (li = 0; li < size; li++) {
            for (ci = 0; ci < size; ci++) {
                int column = 0;
                for (int line = 0; line < size; line++) {
                    // System.out.println("Line_Column: [" + line + "][" + column + "]");
                    if (cells[line][column].value != 0) {
                        column++;
                        continue;
                    }
                    for (column = 0; column < size; column++) {
                        if (cells[line][column].value != 0) {
                            continue;
                        }

                        int value;
                        int whatLine = -1;    // fake value
                        int whatColumn = 0;
                        for (value = 1; value < size * size; value++) {
                            if (cells[li + line][ci + column].possibleValues[value - 1] != 0) {
                                if (whatLine == -1) {
                                    whatLine = li + line;
                                    whatColumn = ci + column;
                                } else {  //if value appears second time - skip it
                                    whatLine = -1;
                                    break; // out from loop
                                }
                            }
                        }
                        if (whatLine == -1) {
                        } else {
                            if (SetValue(whatLine, whatColumn, value) < 0) // Set value into the cell
                            {
                                return -1;
                            }
                            ret++;
                        }
                    }
                }

            }
        }
        if (ret != 0) {
            DisplayPoss("After Solve3");
        }
        System.out.println();
        System.out.println(" Solve3 returns " + ret);
        return ret;
    }
    /*
     * Function is searching for cells in lines, columns & quadrants which have
     * two NumberOfPossVal & delegate its value to FindRegion()
     */

    int Solve4() {
        System.out.println("Solve4 starts");
        int line;
        int column;
        int ret = 0;

        // printf("Function Solve4\n");
        // Try to find pairs. For every cell with 2 possible values scan
        // line, column and quad
        for (line = 0; line < size * size; ++line) {
            for (column = 0; column < size * size; ++column) {
                if (cells[line][column].NumberOfPossVal == 2) {

                    // Scan the line
                    ret += FindRegion(line, column, line, 0, line + 1, size * size);

                    // Scan the column
                    ret += FindRegion(line, column, 0, column, size * size, column + 1);

                    // Scan the quad
                    int top, left, bottom, right;
                    top = line - (line % size);
                    left = column - (column % size);
                    bottom = top + size;
                    right = left + size;
                    ret += FindRegion(line, column, top, left, bottom, right);
                }
            }
        }

        System.out.println();
        System.out.println(" Solve4 returns " + ret);
        return ret;
    }
//***************************************************************
  /*
     * Function is searching for cells with two same possibleValues & calls
     * FixThePair()
     */

    int FindRegion(int line, int column, int top, int left, int bottom, int right) {
        int ret = 0;

        // Scan the region and try to find a cell with the same poss values
        int li, cl, ii;
        for (li = top; li < bottom; li++) {
            for (cl = left; cl < right; ++cl) {
                // If the cell is the same cell or it has more than 2 posvals
                // - skip it
                if ((li == line && cl == column)
                        || cells[li][cl].value != 0
                        || cells[li][cl].NumberOfPossVal != 2) {
                    continue;
                }

                // Does the cell have the same posvals?
                boolean theSame = true;
                for (ii = 0; ii < size; ii++) {
                    if ((cells[li][cl].possibleValues[ii])
                            != (cells[line][column].possibleValues[ii])) {
                        theSame = false;
                        break;
                    }
                }

                if (!theSame) {
                    continue;
                }

                // We found a pair
                ret += FixThePair(line, column, li, cl, top, left, bottom, right);
            }
        }

        return ret;
    }
//************************************************************************************
  /*
     * This function scans the region (top,left:bottom,right) and is looking for
     * cells where number of possible values is greater than 2 and it has
     * possible values from cell (line,column) removes those possible values
     * from this cell
     */

    int FixThePair(int line, int column, int li, int cl,
            int top, int left, int bottom, int right) {
        int ll, cc;
        int ret = 0;
        int ii;

        for (ll = top; ll < bottom; ++ll) {
            for (cc = left; cc < right; ++cc) {

                // We should skip the cells where the value is already set
                if (cells[ll][cc].value > 0) {
                    continue;
                }

                // We should also skip two cells: (line,column) and (li, cl)
                if ((li == ll && cl == cc)
                        || (ll == line && cc == column)) {
                    continue;
                }

                // If this cell has one of the pair values - remove it
                for (ii = 0; ii < size * size; ++ii) {
                    if (cells[line][column].possibleValues[ii] == 0) {
                        continue;
                    }
                    if (UpdatePossValuesForCell(ll, cc, ii + 1) != 0) {
                        // printf("Line=%d, col=%d, Value %d removed from pos.val. of cell (%d,%d)\n",
                        //       line, column, ii+1, ll, cc);
                        ret++;
                    }
                }
            }
        }

        return ret;
    }

    int SolveAll(int level) {
        int line, column;
        int n;
        int ret = 0;
        /*
          while ((n = Solve()) != 0 || (n = Solve1()) != 0 || (n = Solve2()) !=
          0 || (n = Solve3()) != 0 || (n = Solve4()) != 0); { if (n < 0) {
          return -1; } }
         */
         
        for (line = 0; line < size; ++line) {
            for (column = 0; column < size; ++column) {
                // We should skip the cells where the value is already set
                if (cells[line][column].value != 0) {
                    continue;
                }
                // We found undefined value. If we can't solve it - we'll return non-zero
                ret++;

                int ii;
                for (ii = 0; ii < size; ii++) {

                    if (cells[line][column].possibleValues[ii] == 0) {
                        continue;
                    }
                    Matrix workingCopy = new Matrix(this);  // create new object workingCopy 
                                                            // construcctor Matrix(Matrix from);

                    //Set value and try to solve..
                    if (workingCopy.SetValue(line, column, ii + 1) < 0) {
                        System.out.println("Can't set " + ii + 1 + "to " + "[" + line + "][" + column + " Try another one");
                        continue;
                    }
                    if (workingCopy.SolveAll(level+1) == 0) // Solution is found
                    {
                        copyMatrix(workingCopy);
                        return 0;
                    }
                }
                // We tried all possible values for the line:column and nothing fits - so no solution here
                return ret;
            }
        }
        // If we didn't find any cell with undefined value, ret will remain 0 - it means success
        return ret;
    }

    int Display(String msg, int printPB) {
        int ii, jj;
        System.out.println(" " + msg);
        for (ii = 0; ii < size * size; ++ii) {
            for (jj = 0; jj < size * size; jj++) {
                int nPos;
                int posInd;
                for (nPos = 0, posInd = 0; posInd < size * size; posInd++) {
                    if (cells[ii][jj].possibleValues[posInd] != 0) {
                        nPos++;
                    }


                }

                if (printPB != 0) {
                    if (cells[ii][jj].value != 0) {
                        System.out.printf("    " + cells[ii][jj].value);
                    } else {
                        System.out.printf("   " + cells[ii][jj].value + " " + nPos);
                    }

                } else {
                    System.out.printf("   " + cells[ii][jj].value);
                }


            }
            System.out.println();
        }
        System.out.println("====================================================");
        return 0;
    }

    void DisplayPoss(String msg) {
        int ii, jj;
        System.out.println(" " + msg);
        for (ii = 0; ii < size * size; ++ii) {
            if ((ii % size) == 0) {
                System.out.println();
            }
            for (jj = 0; jj < size * size; jj++) {
                if ((jj % size) == 0) {
                    System.out.print(" | ");
                }
                if (cells[ii][jj].value == 0) {
                    int posInd;
                    for (posInd = 0; posInd < size * size; posInd++) {
                        if (cells[ii][jj].possibleValues[posInd] != 0) {
                            System.out.print("" + (posInd + 1));
                        } else {
                            System.out.print("");
                        }
                    }
                } else {
                    System.out.print(" " + cells[ii][jj].value);
                }
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println("====================================================");
        //return;
    }

    int CheckFinalMatrix() {
        ////////////CheckLines///////////////////////
        int line, column;
        for (line = 0; line < size * size; ++line) {
            int LineSum = 0;
            for (column = 0; column < size * size; ++column) {
                LineSum += cells[line][column].value;  // Sum values in the each line
            }
            if (LineSum != ((1 + size * size) * size * size) / 2) {
                System.out.println("Error!! In Line: " + "[" + line + 1 + "]" + " LineSum: " + LineSum);
            }
        }
        ////////////CheckColumn/////////////////////
        for (column = 0; column < size * size; ++column) {
            int ColumnSum = 0;
            for (line = 0; line < size * size; ++line) {
                ColumnSum += cells[line][column].value;  // Sum values in the each column
            }
            if (ColumnSum != ((1 + size * size) * size * size) / 2) {
                System.out.println("Error!! In Column: " + "[" + column + 1 + "]" + "ColumnSum: " + ColumnSum);
            }
        }
        ////////////CheckQuadrant///////////////////
        int li, cl;
        int top, left, bottom, right;
        int QuadSum;
        //Scan all matrix
        for (line = 0; line < size; ++line) {
            for (column = 0; column < size; ++column) {
                top = line * size;
                left = column * size;
                bottom = top + size;
                right = left + size;
                QuadSum = 0;
                // Calculate summ for every quadrant
                for (li = top; li < bottom; ++li) {
                    for (cl = left; cl < right; ++cl) {
                        QuadSum += cells[li][cl].value;
                        if (QuadSum == ((1 + size) * size) / 2) {
                        }
                    }
                }
            }

        }
        return 0;
    }
}


// Just a comment to check github
