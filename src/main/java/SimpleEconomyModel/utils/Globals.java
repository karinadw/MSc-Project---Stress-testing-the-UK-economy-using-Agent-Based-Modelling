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

    @Input(name = "Percentage Firms Micro")
    public double percentMicroFirms = 0.95;

    @Input(name = "Percentage Firms Small/Medium")
    public double percentSmallFirms = 0.04;

    @Input(name = "Number of Sectors")
    public int nbSectors = 10;

    //TODO: check this
    @Input(name = "Multiplier for production")
    public int productionConstant = 1;

    @Input
    public double c = 0.2d; // this is for calculating the consumption budget

    @Input
    //TODO: refactor this as I have named the productivity of a firm alpha
    public double delta = 0.02d; // this is used for calculating the dividend for investors (dividend = alpha * profit)

    // TODO: copied the number from Mark 0 model
    @Input(name = "ettaPlus")
    public double etta_plus = 0.416d;

    // TODO: copied the number from Mark 0 model
    @Input(name = "ettaMinus")
    public double etta_minus = 0.12d;

    // TODO: copied the number from Mark 0 model
    @Input(name = "gammaP")
    public double gamma_p = 0.05d;

    // TODO: copied the number from Mark 0 model
    @Input(name="Theta")
    public double Theta = 1.5d;

    @Input(name = "mu")
    public double mu = 1.0;
    @Input(name="gammaW")
    public double gamma_w = 0.05d;
    public int nbGoods = nbSectors;

    public HashMap<Integer, Long> goodExchangeIDs;

    @Input(name="f")
    public double f = 1.0d;

    @Input(name="phi")
    public double phi = 0.1d;

    @Input
    public double nbExclusiveGoods = 0.2d; // its a percentage
    @Input
    public double percentageWealthyHouseholds = 0.1;
    @Input
    public long initialSaving = 10000;
    @Input
    public long initialSavingRich = 35000;
    @Input
    public int deposistsMultiplier = 2;
    public double[][] weightsArray;
    public int[][] householdGoodWeights = {{1}, {1}, {1}, {1}, {1}, {1}, {1}, {1}, {1}, {1}}; // if changing this remember to change to value below
    public int nbGoodsHouseholds = 10; // do not change this without changing the above

    // shock variables
    @Input
    public int sectorShock = 0;
    @Input
    public double productionShock = 1; // default is 1 which means production stays the same


    // debugging
    public int unemployedCounter = 0;
    @Input
    public double probabilityOfPurchasing = 0.7;
    public int totalVacancies = 0;
    public int nonProductiveFirms= 0;


}

