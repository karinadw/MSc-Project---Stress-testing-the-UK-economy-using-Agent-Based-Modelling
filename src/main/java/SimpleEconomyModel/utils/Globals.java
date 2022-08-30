package SimpleEconomyModel.utils;

import simudyne.core.abm.GlobalState;
import simudyne.core.annotations.Input;
import simudyne.core.annotations.Variable;

import java.util.HashMap;


public class Globals extends GlobalState {

    @Input(name = "Number of Firms")
    public long nbFirms = 500;

    @Input(name = "Size of Workforce")
    public long nbWorkers = 2000;
    public double percentMicroFirms = 0.95;
    public double percentSmallFirms = 0.04;
    public int nbSectors = 10;
    public int productionConstant = 5;

    @Input
    public double c = 0.2d; // this is for calculating the consumption budget

    @Input
    public double delta = 0.02d; // this is used for calculating the dividend for investors (dividend = alpha * profit)
    public double etta_plus = 0.416d;
    public double etta_minus = 0.12d;
    @Input(name = "gammaP")
    public double gamma_p = 0.05d;
    @Input(name="Financial fragility threshold")
    public double Theta = 1.5d;

    public double mu = 1.0;
    @Input(name="gammaW")
    public double gamma_w = 0.05d;
    public int nbGoods = nbSectors;

    public HashMap<Integer, Long> goodExchangeIDs;

    @Input(name="Probability of being purchased")
    public double f = 0.0d;

    @Input(name="Percentage chance of revival")
    public double phi = 0.1d;
    public double percentageWealthyHouseholds = 0.1;
    public int deposistsMultiplier = 2;
    public double[][] weightsArray;
    public int[][] householdGoodWeights = {{1}, {1}, {1}, {1}, {1}, {1}, {1}, {1}, {1}, {1}}; // if changing this remember to change to value below
    public int nbGoodsHouseholds = 10; // do not change this without changing the above

    // shock variables
    @Input
    public int sectorShock = 0;
    @Input
    public double productionShock = 0; // default is 0 which means production stays the same


    // debugging
    public int unemployedCounter = 0;
    public double probabilityOfPurchasing = 0.7;
    public int totalVacancies = 0;


}

