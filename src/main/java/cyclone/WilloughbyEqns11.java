package cyclone;

import java.util.ArrayList;

import static cyclone.Parameters.TOLERANCE;


public class WilloughbyEqns11 extends TropicalCycloneModel {


    /**
     * need to provide direction and speed
     * @param cellSize
     * @param FileOuputPath
     * @param bounds
     */
    public WilloughbyEqns11(float cellSize, String FileOuputPath, ArrayList<Float> bounds){
        super(cellSize, FileOuputPath, bounds);

    }

    public void runModel(){




    }


    /**
     * polynomial weight bellramp function.
     * @param x
     * @param val
     * @return
     */
    private float wf(float x, float val){

        // powers of x-value
        float x5 = x*x*x*x*x;
        float x6 = x*x*x*x*x*x;
        float x7 = x*x*x*x*x*x*x;
        float x8 = x*x*x*x*x*x*x*x;
        float x9 = x*x*x*x*x*x*x*x*x;

        //solution
        float soln = 126 * x5 - 420 * x6 + 540 * x7 - 315 * x8 + 70 * x9 - val;

        return soln;
    }

    /**
     * Derivative of weighted bellramp function.
     * @param x
     * @param val
     * @return
     */
    private float dwf(float x, float val){

        // powers of x-value
        float x4 = x*x*x*x;
        float x5 = x*x*x*x*x;
        float x6 = x*x*x*x*x*x;
        float x7 = x*x*x*x*x*x*x;
        float x8 = x*x*x*x*x*x*x*x;

        //solution
        float soln = 630 * x4 - 2520 * x5 + 3780 * x6 - 2520 * x7 + 630 * x8;


        return soln;
    }

}
