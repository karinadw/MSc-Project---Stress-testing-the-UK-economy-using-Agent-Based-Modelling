package SimpleEconomyModel;

import SimpleEconomyModel.agents.*;
import SimpleEconomyModel.utils.Globals;
import SimpleEconomyModel.utils.HealthyFirmAccount;
import SimpleEconomyModel.utils.Links;
import simudyne.core.abm.AgentBasedModel;
import simudyne.core.abm.Group;
import simudyne.core.abm.Split;
import simudyne.core.annotations.ModelSettings;

import java.util.ArrayList;
import java.util.HashMap;


public class SimpleEconomyModel extends AgentBasedModel<Globals> {

    @Override
    public void init() {

        createDoubleAccumulator("inflation");
        createDoubleAccumulator("employment");
        createDoubleAccumulator("unemployment");

        registerAgentTypes(Firm.class,
                Household.class,
                Economy.class,
                GoodsMarket.class
        );

        registerLinkTypes(Links.HouseholdToEconomy.class,
                Links.FirmToEconomyLink.class,
                Links.FirmToWorkerLink.class,
                Links.WorkerToFirmLink.class,
                Links.FirmToInvestorLink.class,
                Links.InvestorToFirmLink.class,
                Links.FirmToBuyerLink.class,
                Links.EconomyToFirm.class,
                Links.GoodsMarketToEconomy.class);
    }

    int i = 0;
    int householdNumber = 0;
    int householdRich = 0;


    @Override
    public void setup() {

        getGlobals().goodExchangeIDs = new HashMap<>();

        Group<GoodsMarket> goodsMarket = generateGroup(GoodsMarket.class, getGlobals().nbGoods, goodsVariable -> {
            goodsVariable.householdsDemandingGoods = new ArrayList<>();
            goodsVariable.priceOfGoods = new HashMap<>();
            goodsVariable.firmsSupplyingGoods = new ArrayList<>();
            goodsVariable.firmsDemandingIntermediateGoods = new ArrayList<>();
            goodsVariable.goodTraded = i; // the ID of the good when the agent is being created
            getGlobals().goodExchangeIDs.put(i, goodsVariable.getID()); // so that the good can be accessed from globals instead of adding links
            i++;
        });

        Group<Firm> FirmGroup = generateGroup(Firm.class, getGlobals().nbFirms, firm -> {
            firm.sector = firm.getPrng().getNextInt(getGlobals().nbSectors); // get next int creates a random variable until limit - 1, so with this range we get values from 0 to 20
            firm.determineSizeOfCompany();
            firm.isProductive = true;
            firm.isHiring = true;
            firm.stockOfIntermediateGood = new HashMap<Integer, Double>();
            firm.IntermediateGoodNeeded = new HashMap<Integer, Double>();
        });



        Group<Household> HouseholdGroup = generateGroup(Household.class, getGlobals().nbWorkers, household -> {
            household.sector_skills = household.getPrng().getNextInt(getGlobals().nbSectors);  // random sector skills applied to the workers
            // skewed distribution of wealth
            // reference for number used for saving -> initial wealth: https://www.ons.gov.uk/peoplepopulationandcommunity/personalandhouseholdfinances/incomeandwealth/bulletins/distributionofindividualtotalwealthbycharacteristicingreatbritain/april2018tomarch2020
            household.budget = new HashMap<Integer, Double>();

            // TODO: Check the variables here : percentage of rich households and percentage of exclusive goods
            if (householdNumber < (getGlobals().nbWorkers - Math.ceil(getGlobals().percentageWealthyHouseholds * getGlobals().nbWorkers))) {
                // common individuals
                household.rich = false;
//                household.wealth = getGlobals().initialSaving;
                household.wealth = household.getPrng().uniform(100000.00, 500000.00).sample();
            } else {

                // wealthy individuals
                household.rich = true;
                householdRich++;
//                household.wealth = getGlobals().initialSavingRich;
                household.wealth = household.getPrng().uniform(500000.00, 4000000.00).sample();
                }
            double moneyToSpend = getGlobals().c * household.wealth;
            // wealthy individuals spend on luxury goods as well
            // they spend on all goods

            if (!household.rich) {
                double rand = household.getPrng().uniform(0, 1).sample();
                for (int j = 0; j < getGlobals().nbGoods; j++) {
                    if (getGlobals().householdGoodWeights[j][0] == 1 && rand < getGlobals().probabilityOfPurchasing) {
                        household.budget.put(j, moneyToSpend / getGlobals().nbGoodsHouseholds);
                    }
                }
            } else{
                for (int j = 0; j < getGlobals().nbGoods; j++) {
                    if (getGlobals().householdGoodWeights[j][0] == 1) {
                        household.budget.put(j, moneyToSpend / getGlobals().nbGoodsHouseholds);
                    }
                }
            }
            householdNumber++;
            household.unemploymentBenefits = (61.05 + 77.00); // average of above and below 24 years, not dividing by 2 because this is received every 2 weeks.
            household.productivity = household.getPrng().uniform(0.1, 1).sample();
        });


        Group<Economy> Economy = generateGroup(Economy.class, 1, market -> {
            market.firmsHiring = new ArrayList<>();
            market.availableWorkers = new ArrayList<>();
            market.priceOfGoods = new HashMap<Double, Double>();
            market.priceOfGoodsDemanded = new HashMap<Double, Double>();
            market.healthyFirmAccountMap = new HashMap<Long, HealthyFirmAccount>();
            market.indebtedFirmsMap = new HashMap<>();
            market.bankruptFirmsArray = new ArrayList<>();
            market.bailedOutFirmsMap = new HashMap<>();
        });

        HouseholdGroup.fullyConnected(Economy, Links.HouseholdToEconomy.class);
        FirmGroup.fullyConnected(Economy, Links.FirmToEconomyLink.class);
        Economy.fullyConnected(FirmGroup, Links.EconomyToFirm.class);
        Economy.fullyConnected(goodsMarket, Links.GoodsMarketToEconomy.class);

        super.setup();
    }

    @Override
    public void step() {
        super.step();

        System.out.println("Tick: " + getContext().getTick());

        // Initial settings that do not need to get repeated throughout
        if (getContext().getTick() == 0) {

            // set the initial weights that relate the firms for the intermediate goods
            run(Economy.InitWeights());

            // firms set their vacancies according to their size
            run(Firm.SetVacancies());

            //the firm sets the prices of the goods it produces
            run(Firm.SetPriceOfGoods());

            run(Firm.SetWages());

            // setting all firms with an initial stock of intermediate goods so that every firm can produce
            run(Firm.SetInitialStockOfIntermediateGoods());

            // set sector specific wages and sector specific pricing of goods
//            run(Firm.sendInfo(), Economy.SetFirmProperties(), Firm.SetSectorSpecifics());
            run(Firm.SetSectorSpecificGoods());

            // dividing households into investors and workers
            run(
                    Split.create(Household.ApplyForInvestor(),
                            Firm.FindInvestors()),
                    Economy.AssignInvestorToFirm(),
                    Split.create(
                            Household.DetermineStatus(),
                            Firm.AssignFirmInvestor()
                    )
            );
        }

        // workers apply for jobs and firms that have vacancies hire
        // firms decide what good they produce depending on the sector they're in
        run(
                Split.create(
                        // if the worker is unemployed, the worker applies for a job. Received by the economy
                        Household.applyForJob(),
                        // if the firm is hiring, vacancies are sent to the economy
                        Firm.sendInfo()
                ),
                Economy.MatchFirmsAndWorkers(),
                Split.create(
                        // if the worker has been employed it updates its status
                        Household.updateAvailability(),
                        Firm.updateVacancies()
                )
        );

        // the productivity of the firm is dependant on the productivity of the workers
        run(Household.sendProductivity(), Firm.CalculateFirmProductivity());

        // Firms produce their good according to the productivity of the firm, the number of workers and the size of the firm
        run(Firm.FirmsProduce());

        // calculates the average price of products and sends it to the firms
        // this is needed for the firm to update its strategy
        // change in average price is used to calculate inflation



        // workers get paid the wage offered by their firm and investors get paid dividends
        run(Firm.payWorkers(), Household.receiveIncome());

        run(
                Split.create(
                        Household.sendDemand(),
                        Firm.sendSupplyAndDemand()),
                GoodsMarket.matchSupplyAndDemand(),
                Split.create(
                        Firm.receiveDemandAndIntermediateGoods(),
                        Household.updateFromPurchase()
                )
        );

        // calculate average price of sold goods
        run(Firm.sendPrice(), Economy.GetPrices());
        run(Economy.CalculateAndSendAveragePrice(), Firm.GetAveragePrice());

        // calculates inflation
        // not done in the first tick as there is no information in product pricing yet
        if (getContext().getTick() > 0) {
            run(Economy.CalculateInflation());
        }

        run(
                Split.create(
                        Firm.sendInfoToGetAvailableWorkers(),
                        Household.SendUnemployment()
                ),
                Economy.calculateUnemploymentAndAvailableWorkers(),
                Firm.receiveAvailableWorkers()
        );

        // after households purchase, the update their consumption budget for each good
        run(Household.updateConsumptionBudget());


        // TODO: re-write those functions that are same as Mark 0
        // Firm accounting
        // if firm can (earnings and profits are positive), it will pay out dividends to investor
        run(Firm.Accounting(), Household.getDividends());
        run(Firm.sendHealthyFirmAccount(), Economy.receiveHealthyFirmAccounts());

        // Checking for firm defaults
        run(Firm.sendBailoutRequest(), Economy.receiveIndebtedFirmDebt());
        run(Economy.checkDefaults(), Firm.paymentOfIndebtedFirm());
        run(Economy.sendBailoutPackages(), Firm.receiveBailoutPackage());
        run(Economy.sendBankruptcyMessages(), Firm.receiveBankruptcyMessage());

        // Revival of firms
        run(Firm.doRevival(), Household.ReviveFirm());

        //update the target production to meet the demand
        run(Firm.adjustPriceProduction());

        if (getContext().getTick() > 12) {
            run(Firm.productionShock());
        }

        // Once it has an updated target production it can check if it needs more intermediate goods
        run(Firm.CheckIfIntGoodsNeeded());

        // Update firms' vacancies according to the new target production
        run(Firm.UpdateVacancies());

        // check how long the worker has been unemployed
        run(Household.checkLengthOfUnemployment());

        // with the updated vacancies, the firms fire if they don't need any more worker's
        // worker's that had already applied can now apply in the next hiring round
        run(Household.JobCheck(), Firm.FireWorkers(), Household.CheckIfFired());
        run(Household.UnemployedWorkerCanApply());

        // hiring round
        // workers apply for jobs and firms that have vacancies hire
        // firms decide what good they produce depending on the sector they're in
        run(
                Split.create(
                        // if the worker is unemployed, the worker applies for a job. Received by the economy
                        Household.applyForJob(),
                        // if the firm is hiring, vacancies are sent to the economy
                        Firm.sendInfo()
                ),
                Economy.MatchFirmsAndWorkers(),
                Split.create(
                        // if the worker has been employed it updates its status
                        Household.updateAvailability(),
                        Firm.updateVacancies()
                )
        );


        // check if the worker can upgrade its skills
        run(Household.UpgradeSkills());

        // firms can change sizes depending on the employees it has
        run(Firm.UpdateFirmSize());

        // for debugging purposes
        run(Firm.checkProductiveFirms());
    }

}


