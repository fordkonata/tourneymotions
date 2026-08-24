package backend;


import backend.dbconnector.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


/***TODO AFTER INITIAL TESTING COMPONENTS IS COMPLETE
*  Try Modules/libraries:
* Project Lombok
* Mockito - for mock objects integration testing
* Apache Commons - for common operations and general arithmetic
*  Google Guava - for collections multimaps and such
* Hibernate - for database Java class mappings
* (Late Game) - Spring framework - for launch support
 */



//Path to database: C:\Users\solac\IdeaProjects\autoseederreal\src\db\HCF_TournamentDB.sql
public class Main {


    public static void main(String[] args) throws SQLException {//TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        BufferedReader buffer  = new BufferedReader((new InputStreamReader(System.in)));
        HashMap<String, Tournament> tournamentMap = new HashMap<>();
        JdbcConnector jdbcConnector = new JdbcConnector();
        TournamentQueries tournamentQueries = new TournamentQueries(jdbcConnector.getPrimeConnection());
        BracketQueries bracketQueries = new BracketQueries(jdbcConnector.getPrimeConnection());
        PoolQueries poolQueries = new PoolQueries(jdbcConnector.getPrimeConnection());
        MatchQueries matchQueries = new MatchQueries(jdbcConnector.getPrimeConnection());
        PlayerQueries playerQueries = new PlayerQueries(jdbcConnector.getPrimeConnection());
        ArrayList<Player> playerList = populatePlayerList(playerQueries);

        try {
//        insertToDB(playerList);

            mainLoop:
            while (true) {
                System.out.printf("**********************************\nWelcome to the HCF Tournament Seeder!!\n**********************************\n\n\n\n");
                System.out.println("Main Menu \n");

                try {
                    System.out.println("1. Create a new Tournament\n ");
                    System.out.println("2. Edit a Tournament\n");
                    System.out.println("3. Show all Tournaments\n");
                    System.out.println("4. Show all players\n");
                    System.out.println("0. Exit");
                    String inputString = buffer.readLine();
                    switch (inputString) {
                        case "1":
                            System.out.println("Enter Tournament Name: \n");
                            String tLine = null;

                            try { tLine = buffer.readLine(); }
                            catch (IOException e) { throw new RuntimeException(e); }

                            Tournament newTournament = new Tournament(tLine, tournamentQueries);
                            tournamentMap.put(tLine, newTournament);
                            System.out.println("Created new tournament: " + tLine); // this might end up as null?
                            break;
                        case "2":
                            System.out.println("Enter Tournament Name: \n");
                            String tString = null;

                            try { tString = buffer.readLine();}
                            catch (IOException e) { throw new RuntimeException(e); }

                            Tournament retrievedTournament = tournamentMap.get(tString);

                            if (retrievedTournament == null) {
                                System.out.println("Tournament not found!");
                                break;
                            }
                            boolean editLoop = true;
                            while (editLoop) {
                                System.out.println("1. Show bracket status.");
                                System.out.println("2. Resolve a match");
                                System.out.println("3. Create a Bracket.");
                                System.out.println("4. Show a game's bracket.");
                                System.out.println("5. Simulate a Bracket.");
                                System.out.println("0. Back to main menu.");
                                String inputString1 = buffer.readLine();
                                switch (inputString1) {
                                    //Resolve a match
                                    case "1":
                                        System.out.println("Enter Bracket Name: ");
                                        String bracketName = null;

                                        try { bracketName = buffer.readLine(); }
                                        catch (IOException e) {
                                            System.out.println("Input invalid!!");
                                            throw new RuntimeException(e);
                                        }

                                        Bracket currBracket = retrievedTournament.findBracket(bracketName);
                                        String editString = buffer.readLine();

                                        //Handles actions on a selected tournament
                                        System.out.println("1. Show all pools.");
                                        System.out.println("2. Search by pool.");
                                        System.out.println("0. Return to previous.");
                                        switch (editString) {
                                            case "1":
                                                System.out.println(currBracket);
                                            case "2":
                                                System.out.println("Enter Stage: ");
                                                String stageFindString = buffer.readLine();

                                                System.out.println("Enter Pool: ");
                                                String poolFindString = buffer.readLine();

                                                Integer stageFindInt = Integer.valueOf(stageFindString);
                                                Integer poolFindInt = Integer.valueOf(poolFindString);
                                                Pool currPool = currBracket.findPool(stageFindInt, poolFindInt);

                                                //changing this to one line for winners/losers and match ID for input
                                                System.out.println("Enter 'Winners' or 'Losers', followed by the Match position ");

                                                System.out.println("Enter Match ID: ");
                                                String matchFindString = buffer.readLine();
                                                Integer matchFindInt = Integer.valueOf(matchFindString);
                                                //currPool.parentbracket.allMatches.get(matchFindInt); //fix this
                                            case "0":
                                                editLoop = false;
                                                break;
                                        }
                                    case "3":
                                        System.out.println("Enter the game name of the bracket: ");
                                        String newBracketLine = null;

                                        try { newBracketLine = buffer.readLine(); }
                                        catch (IOException e) { throw new RuntimeException(e); }

                                        retrievedTournament.createBracket(newBracketLine, playerList, "Street Fighter 6",
                                                bracketQueries, poolQueries, matchQueries);

                                        System.out.println(tournamentMap.get(tString).findBracket(newBracketLine));
                                        System.out.println("Tournament Bracket Created!");
                                        break;
                                    case "4":
                                        System.out.println("Enter the game name: ");
                                        String showGameBracketString = null;

                                        try { showGameBracketString = buffer.readLine(); }
                                        catch (IOException e) { throw new RuntimeException(e); }

                                        Bracket showGameRetrievedBracket = retrievedTournament.findBracket(showGameBracketString);
                                        System.out.println(showGameRetrievedBracket);
                                        break;
                                    case "5":
                                        System.out.println("Enter the game name: ");
                                        String simulateGameBracketString = null;

                                        try { simulateGameBracketString = buffer.readLine(); }
                                        catch (IOException e) { throw new RuntimeException(e); }

                                        Bracket simulateRetrievedBracket = retrievedTournament.findBracket(simulateGameBracketString);
                                        System.out.println("Simulating bracket... ");
                                        int stageCount = 1;
                                        Boolean bracketDone = false;

                                        while (!bracketDone) {
                                            bracketDone = simulateRetrievedBracket.autoCompleteBracket(simulateRetrievedBracket.getBracketStagesMap().get(stageCount));
                                            stageCount++;
                                        }

//                                    System.out.println("All matches size: " + simulateRetrievedBracket.getAllMatchesByID().size());
//                                    System.out.println("Processed matches: ");
//                                    simulateRetrievedBracket.getAllMatchesByID().values().forEach(m -> System.out.println( "Stage: " +
//                                            m.getParentPool().getPoolStage() + " | " + "Pool: " + m.getParentPool().poolName + " | " +  "Side: " +
//                                            m.getMatchSide() + " | " + m .getMatchPosition() + " Winner: " +
//                                            (m.getWinner() != null ? m.getWinner() : "NULL")));
                                        System.out.println(simulateRetrievedBracket.getFinalPlacements());
                                    case "0":
                                        break;
                                }
                            }
                            break;
                        case "3":
                            break;
                        case "4":
                            for (Player player : playerList) System.out.println(player);
                        case "0":
                            System.out.println("Exiting...");
                            Thread.sleep(4000);
                            break mainLoop;
                        default:
                            System.out.println("Please enter a number from 0-4.");
                    }
                }
                catch (IOException | InterruptedException e) { throw new RuntimeException(e); }
            }
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }


    public static ArrayList<Player> populatePlayerList(PlayerQueries playerQueries) {
        ArrayList<Player> playerList = new ArrayList<>();

        Player punk = new Player("Victor", "Punk", playerQueries);
        punk.setPlayerPoints("Street Fighter 6", 2000);
        punk.setPlayerTier("Street Fighter 6");

        Player xiaohai = new Player("", "Xiaohai", playerQueries);
        xiaohai.setPlayerPoints("Street Fighter 6", 2000);
        xiaohai.setPlayerTier("Street Fighter 6");

        Player bigBird = new Player("", "BigBird", playerQueries);
        bigBird.setPlayerPoints("Street Fighter 6", 2000);
        bigBird.setPlayerTier("Street Fighter 6");

        Player endingWalker = new Player("", "EndingWalker", playerQueries);
        endingWalker.setPlayerPoints("Street Fighter 6", 2000);
        endingWalker.setPlayerTier("Street Fighter 6");

        Player camPaine = new Player("", "CamPaine", playerQueries);
        camPaine.setPlayerPoints("Street Fighter 6", 1400);
        camPaine.setPlayerTier("Street Fighter 6");

        Player jackieTan = new Player("Kenny", "JackieTan", playerQueries);
        jackieTan.setPlayerPoints("Street Fighter 6", 1600);
        jackieTan.setPlayerTier("Street Fighter 6");

        Player mizuha = new Player("", "Mizuha", playerQueries);
        mizuha.setPlayerPoints("Street Fighter 6", 1400);
        mizuha.setPlayerTier("Street Fighter 6");

        Player fenritti = new Player("", "Fenritti", playerQueries);
        fenritti.setPlayerPoints("Street Fighter 6", 1400);
        fenritti.setPlayerTier("Street Fighter 6");

        Player ryanHart = new Player("Ryan", "Ryan Hart", playerQueries);
        ryanHart.setPlayerPoints("Street Fighter 6", 1400);
        ryanHart.setPlayerTier("Street Fighter 6");

        Player booce = new Player("", "Booce", playerQueries);
        booce.setPlayerPoints("Street Fighter 6", 1600);
        booce.setPlayerTier("Street Fighter 6");

        Player riddles = new Player("", "Riddles", playerQueries);
        riddles.setPlayerPoints("Street Fighter 6", 1600);
        riddles.setPlayerTier("Street Fighter 6");

        Player kira = new Player("Joey", "Kira", playerQueries);
        kira.setPlayerPoints("Street Fighter 6", 1400);
        kira.setPlayerTier("Street Fighter 6");

        Player dubz = new Player("Tristan", "Dubz", playerQueries);
        dubz.setPlayerPoints("Street Fighter 6", 1200);
        dubz.setPlayerTier("Street Fighter 6");

        Player maltonNEO = new Player("Stephen", "MaltonNEO", playerQueries);
        maltonNEO.setPlayerPoints("Street Fighter 6", 1000);
        maltonNEO.setPlayerTier("Street Fighter 6");

        Player diaphone = new Player("", "Diaphone", playerQueries);
        diaphone.setPlayerPoints("Street Fighter 6", 1400);
        diaphone.setPlayerTier("Street Fighter 6");

        Player metalOrca = new Player("Konata", "MetalOrca", playerQueries);
        metalOrca.setPlayerPoints("Street Fighter 6", 800);
        metalOrca.setPlayerTier("Street Fighter 6");

// Tier 1 - 2000 points
        Player leshar = new Player("", "Leshar", playerQueries);
        leshar.setPlayerPoints("Street Fighter 6", 2000);
        leshar.setPlayerTier("Street Fighter 6");

        Player menaRD = new Player("", "MenaRD", playerQueries);
        menaRD.setPlayerPoints("Street Fighter 6", 2000);
        menaRD.setPlayerTier("Street Fighter 6");

        Player fuudo = new Player("", "Fuudo", playerQueries);
        fuudo.setPlayerPoints("Street Fighter 6", 2000);
        fuudo.setPlayerTier("Street Fighter 6");

        Player kilzyou = new Player("", "Kilzyou", playerQueries);
        kilzyou.setPlayerPoints("Street Fighter 6", 2000);
        kilzyou.setPlayerTier("Street Fighter 6");

        Player blaz = new Player("", "Blaz", playerQueries);
        blaz.setPlayerPoints("Street Fighter 6", 2000);
        blaz.setPlayerTier("Street Fighter 6");

        Player higuchi = new Player("", "Higuchi", playerQueries);
        higuchi.setPlayerPoints("Street Fighter 6", 2000);
        higuchi.setPlayerTier("Street Fighter 6");

        Player yamaguchi = new Player("", "Yamaguchi", playerQueries);
        yamaguchi.setPlayerPoints("Street Fighter 6", 2000);
        yamaguchi.setPlayerTier("Street Fighter 6");

        Player kobayan = new Player("", "Kobayan", playerQueries);
        kobayan.setPlayerPoints("Street Fighter 6", 2000);
        kobayan.setPlayerTier("Street Fighter 6");

        Player dualKevin = new Player("", "Dual Kevin", playerQueries);
        dualKevin.setPlayerPoints("Street Fighter 6", 2000);
        dualKevin.setPlayerTier("Street Fighter 6");

        Player sahara = new Player("", "Sahara", playerQueries);
        sahara.setPlayerPoints("Street Fighter 6", 2000);
        sahara.setPlayerTier("Street Fighter 6");

        Player angryBird = new Player("", "AngryBird", playerQueries);
        angryBird.setPlayerPoints("Street Fighter 6", 2000);
        angryBird.setPlayerTier("Street Fighter 6");

// Tier 2 - 1800 points
        Player problemX = new Player("", "Problem X", playerQueries);
        problemX.setPlayerPoints("Street Fighter 6", 1800);
        problemX.setPlayerTier("Street Fighter 6");

        Player vxbao = new Player("", "Vxbao", playerQueries);
        vxbao.setPlayerPoints("Street Fighter 6", 1800);
        vxbao.setPlayerTier("Street Fighter 6");

        Player phenom = new Player("", "Phenom", playerQueries);
        phenom.setPlayerPoints("Street Fighter 6", 1800);
        phenom.setPlayerTier("Street Fighter 6");

        Player misterCrimson = new Player("", "Mister Crimson", playerQueries);
        misterCrimson.setPlayerPoints("Street Fighter 6", 1800);
        misterCrimson.setPlayerTier("Street Fighter 6");

        Player xian = new Player("", "Xian", playerQueries);
        xian.setPlayerPoints("Street Fighter 6", 1800);
        xian.setPlayerTier("Street Fighter 6");

        Player kawano = new Player("", "Kawano", playerQueries);
        kawano.setPlayerPoints("Street Fighter 6", 1800);
        kawano.setPlayerTier("Street Fighter 6");

        Player itabashiZangief = new Player("", "Itabashi Zangief", playerQueries);
        itabashiZangief.setPlayerPoints("Street Fighter 6", 1800);
        itabashiZangief.setPlayerTier("Street Fighter 6");

        Player zhen = new Player("", "Zhen", playerQueries);
        zhen.setPlayerPoints("Street Fighter 6", 1800);
        zhen.setPlayerTier("Street Fighter 6");

        Player moke = new Player("", "Moke", playerQueries);
        moke.setPlayerPoints("Street Fighter 6", 1800);
        moke.setPlayerTier("Street Fighter 6");

        Player go1 = new Player("", "Go1", playerQueries);
        go1.setPlayerPoints("Street Fighter 6", 1800);
        go1.setPlayerTier("Street Fighter 6");

        Player nl = new Player("", "NL", playerQueries);
        nl.setPlayerPoints("Street Fighter 6", 1800);
        nl.setPlayerTier("Street Fighter 6");

        Player noahTheProdigy = new Player("", "NoahTheProdigy", playerQueries);
        noahTheProdigy.setPlayerPoints("Street Fighter 6", 1800);
        noahTheProdigy.setPlayerTier("Street Fighter 6");

        Player chrisWong = new Player("", "Chris Wong", playerQueries);
        chrisWong.setPlayerPoints("Street Fighter 6", 1800);
        chrisWong.setPlayerTier("Street Fighter 6");

        Player bonchan = new Player("", "Bonchan", playerQueries);
        bonchan.setPlayerPoints("Street Fighter 6", 1800);
        bonchan.setPlayerTier("Street Fighter 6");

        Player shuto = new Player("", "Shuto", playerQueries);
        shuto.setPlayerPoints("Street Fighter 6", 1800);
        shuto.setPlayerTier("Street Fighter 6");

        Player kakeru = new Player("", "Kakeru", playerQueries);
        kakeru.setPlayerPoints("Street Fighter 6", 1800);
        kakeru.setPlayerTier("Street Fighter 6");

        Player dcq = new Player("", "DCQ", playerQueries);
        dcq.setPlayerPoints("Street Fighter 6", 1800);
        dcq.setPlayerTier("Street Fighter 6");

        Player micky = new Player("", "Micky", playerQueries);
        micky.setPlayerPoints("Street Fighter 6", 1800);
        micky.setPlayerTier("Street Fighter 6");

        Player momochi = new Player("", "Momochi", playerQueries);
        momochi.setPlayerPoints("Street Fighter 6", 1800);
        momochi.setPlayerTier("Street Fighter 6");

        Player kusanagi = new Player("", "Kusanagi", playerQueries);
        kusanagi.setPlayerPoints("Street Fighter 6", 1800);
        kusanagi.setPlayerTier("Street Fighter 6");

        Player caba = new Player("", "Caba", playerQueries);
        caba.setPlayerPoints("Street Fighter 6", 1800);
        caba.setPlayerTier("Street Fighter 6");

        Player nephew = new Player("", "Nephew", playerQueries);
        nephew.setPlayerPoints("Street Fighter 6", 1800);
        nephew.setPlayerTier("Street Fighter 6");

        Player nuckledu = new Player("", "Nuckledu", playerQueries);
        nuckledu.setPlayerPoints("Street Fighter 6", 1800);
        nuckledu.setPlayerTier("Street Fighter 6");

        Player ryukichi = new Player("", "Ryukichi", playerQueries);
        ryukichi.setPlayerPoints("Street Fighter 6", 1800);
        ryukichi.setPlayerTier("Street Fighter 6");

        Player tokido = new Player("", "Tokido", playerQueries);
        tokido.setPlayerPoints("Street Fighter 6", 1800);
        tokido.setPlayerTier("Street Fighter 6");

        Player matsu56 = new Player("", "Matsu56", playerQueries);
        matsu56.setPlayerPoints("Street Fighter 6", 1800);
        matsu56.setPlayerTier("Street Fighter 6");

        Player oilKing = new Player("", "Oil King", playerQueries);
        oilKing.setPlayerPoints("Street Fighter 6", 1800);
        oilKing.setPlayerTier("Street Fighter 6");

        Player chrisT = new Player("", "ChrisT", playerQueries);
        chrisT.setPlayerPoints("Street Fighter 6", 1800);
        chrisT.setPlayerTier("Street Fighter 6");

        Player pugera = new Player("", "Pugera", playerQueries);
        pugera.setPlayerPoints("Street Fighter 6", 1800);
        pugera.setPlayerTier("Street Fighter 6");

        Player shine = new Player("", "Shine", playerQueries);
        shine.setPlayerPoints("Street Fighter 6", 1800);
        shine.setPlayerTier("Street Fighter 6");

        Player daigo = new Player("", "Daigo", playerQueries);
        daigo.setPlayerPoints("Street Fighter 6", 1800);
        daigo.setPlayerTier("Street Fighter 6");

// Tier 3 - 1600 points
        Player bravery = new Player("", "Bravery", playerQueries);
        bravery.setPlayerPoints("Street Fighter 6", 1600);
        bravery.setPlayerTier("Street Fighter 6");

        Player bnbbn = new Player("", "BNBBN", playerQueries);
        bnbbn.setPlayerPoints("Street Fighter 6", 1600);
        bnbbn.setPlayerTier("Street Fighter 6");

        Player hotdog29 = new Player("", "Hotdog29", playerQueries);
        hotdog29.setPlayerPoints("Street Fighter 6", 1600);
        hotdog29.setPlayerTier("Street Fighter 6");

        Player takamura = new Player("", "Takamura", playerQueries);
        takamura.setPlayerPoints("Street Fighter 6", 1600);
        takamura.setPlayerTier("Street Fighter 6");

        Player hikaru = new Player("", "Hikaru", playerQueries);
        hikaru.setPlayerPoints("Street Fighter 6", 1600);
        hikaru.setPlayerTier("Street Fighter 6");

        Player mochi = new Player("", "Mochi", playerQueries);
        mochi.setPlayerPoints("Street Fighter 6", 1600);
        mochi.setPlayerTier("Street Fighter 6");

        Player mago = new Player("", "Mago", playerQueries);
        mago.setPlayerPoints("Street Fighter 6", 1600);
        mago.setPlayerTier("Street Fighter 6");

        Player johnTakeuchi = new Player("", "John Takeuchi", playerQueries);
        johnTakeuchi.setPlayerPoints("Street Fighter 6", 1600);
        johnTakeuchi.setPlayerTier("Street Fighter 6");

        Player haitani = new Player("", "Haitani", playerQueries);
        haitani.setPlayerPoints("Street Fighter 6", 1600);
        haitani.setPlayerTier("Street Fighter 6");

        Player craime = new Player("", "Craime", playerQueries);
        craime.setPlayerPoints("Street Fighter 6", 1600);
        craime.setPlayerTier("Street Fighter 6");

        Player torimeshi = new Player("", "Torimeshi", playerQueries);
        torimeshi.setPlayerPoints("Street Fighter 6", 1600);
        torimeshi.setPlayerTier("Street Fighter 6");

        Player tachikawa = new Player("", "Tachikawa", playerQueries);
        tachikawa.setPlayerPoints("Street Fighter 6", 1600);
        tachikawa.setPlayerTier("Street Fighter 6");

        Player juicyJoe = new Player("", "JuicyJoe", playerQueries);
        juicyJoe.setPlayerPoints("Street Fighter 6", 1600);
        juicyJoe.setPlayerTier("Street Fighter 6");

        Player idom = new Player("", "Idom", playerQueries);
        idom.setPlayerPoints("Street Fighter 6", 1600);
        idom.setPlayerTier("Street Fighter 6");

        Player salvatore = new Player("", "Salvatore", playerQueries);
        salvatore.setPlayerPoints("Street Fighter 6", 1600);
        salvatore.setPlayerTier("Street Fighter 6");

        Player yanai = new Player("", "Yanai", playerQueries);
        yanai.setPlayerPoints("Street Fighter 6", 1600);
        yanai.setPlayerTier("Street Fighter 6");

        Player elChokotay = new Player("", "ElChokotay", playerQueries);
        elChokotay.setPlayerPoints("Street Fighter 6", 1600);
        elChokotay.setPlayerTier("Street Fighter 6");

        Player jak = new Player("", "JAK", playerQueries);
        jak.setPlayerPoints("Street Fighter 6", 1600);
        jak.setPlayerTier("Street Fighter 6");

        Player chrisCCH = new Player("", "ChrisCCH", playerQueries);
        chrisCCH.setPlayerPoints("Street Fighter 6", 1600);
        chrisCCH.setPlayerTier("Street Fighter 6");

        Player hurricane = new Player("", "Hurricane", playerQueries);
        hurricane.setPlayerPoints("Street Fighter 6", 1600);
        hurricane.setPlayerTier("Street Fighter 6");

        Player acqua = new Player("", "Acqua", playerQueries);
        acqua.setPlayerPoints("Street Fighter 6", 1600);
        acqua.setPlayerTier("Street Fighter 6");

        Player dakCorgi = new Player("", "DakCorgi", playerQueries);
        dakCorgi.setPlayerPoints("Street Fighter 6", 1600);
        dakCorgi.setPlayerTier("Street Fighter 6");

        Player armperor = new Player("", "Armperor", playerQueries);
        armperor.setPlayerPoints("Street Fighter 6", 1600);
        armperor.setPlayerTier("Street Fighter 6");

        Player lexx = new Player("", "Lexx", playerQueries);
        lexx.setPlayerPoints("Street Fighter 6", 1600);
        lexx.setPlayerTier("Street Fighter 6");

        Player cosa = new Player("", "Cosa", playerQueries);
        cosa.setPlayerPoints("Street Fighter 6", 1600);
        cosa.setPlayerTier("Street Fighter 6");

        Player enzoTheHokage = new Player("", "EnzoTheHokage", playerQueries);
        enzoTheHokage.setPlayerPoints("Street Fighter 6", 1600);
        enzoTheHokage.setPlayerTier("Street Fighter 6");

        Player sayff = new Player("", "Sayff", playerQueries);
        sayff.setPlayerPoints("Street Fighter 6", 1600);
        sayff.setPlayerTier("Street Fighter 6");

        Player xerna = new Player("", "Xerna", playerQueries);
        xerna.setPlayerPoints("Street Fighter 6", 1600);
        xerna.setPlayerTier("Street Fighter 6");

        Player psycho = new Player("", "Psycho", playerQueries);
        psycho.setPlayerPoints("Street Fighter 6", 1600);
        psycho.setPlayerTier("Street Fighter 6");

        Player kazunoko = new Player("", "Kazunoko", playerQueries);
        kazunoko.setPlayerPoints("Street Fighter 6", 1600);
        kazunoko.setPlayerTier("Street Fighter 6");

        Player broski = new Player("", "Broski", playerQueries);
        broski.setPlayerPoints("Street Fighter 6", 1600);
        broski.setPlayerTier("Street Fighter 6");

        Player notPedro = new Player("", "NotPedro", playerQueries);
        notPedro.setPlayerPoints("Street Fighter 6", 1600);
        notPedro.setPlayerTier("Street Fighter 6");

        Player hibiki = new Player("", "Hibiki", playerQueries);
        hibiki.setPlayerPoints("Street Fighter 6", 1600);
        hibiki.setPlayerTier("Street Fighter 6");

        Player nemo = new Player("", "Nemo", playerQueries);
        nemo.setPlayerPoints("Street Fighter 6", 1600);
        nemo.setPlayerTier("Street Fighter 6");

        Player kojiKog = new Player("", "KojiKog", playerQueries);
        kojiKog.setPlayerPoints("Street Fighter 6", 1600);
        kojiKog.setPlayerTier("Street Fighter 6");

        Player nyChrisG = new Player("", "NYChrisG", playerQueries);
        nyChrisG.setPlayerPoints("Street Fighter 6", 1600);
        nyChrisG.setPlayerTier("Street Fighter 6");

        Player akira = new Player("", "Akira", playerQueries);
        akira.setPlayerPoints("Street Fighter 6", 1600);
        akira.setPlayerTier("Street Fighter 6");

        Player jb = new Player("", "JB", playerQueries);
        jb.setPlayerPoints("Street Fighter 6", 1600);
        jb.setPlayerTier("Street Fighter 6");

        Player joeUmerogan = new Player("", "Joe Umerogan", playerQueries);
        joeUmerogan.setPlayerPoints("Street Fighter 6", 1600);
        joeUmerogan.setPlayerTier("Street Fighter 6");

        Player zangiefBolado = new Player("", "Zangief Bolado", playerQueries);
        zangiefBolado.setPlayerPoints("Street Fighter 6", 1600);
        zangiefBolado.setPlayerTier("Street Fighter 6");

        Player akainu = new Player("", "Akainu", playerQueries);
        akainu.setPlayerPoints("Street Fighter 6", 1600);
        akainu.setPlayerTier("Street Fighter 6");

        Player stealth = new Player("", "Stealth", playerQueries);
        stealth.setPlayerPoints("Street Fighter 6", 1600);
        stealth.setPlayerTier("Street Fighter 6");

        Player kincho = new Player("", "Kincho", playerQueries);
        kincho.setPlayerPoints("Street Fighter 6", 1600);
        kincho.setPlayerTier("Street Fighter 6");

        Player joey = new Player("", "Joey", playerQueries);
        joey.setPlayerPoints("Street Fighter 6", 1600);
        joey.setPlayerTier("Street Fighter 6");

        Player dogura = new Player("", "Dogura", playerQueries);
        dogura.setPlayerPoints("Street Fighter 6", 1600);
        dogura.setPlayerTier("Street Fighter 6");

        Player kingsvega = new Player("", "Kingsvega", playerQueries);
        kingsvega.setPlayerPoints("Street Fighter 6", 1600);
        kingsvega.setPlayerTier("Street Fighter 6");

        Player urielVelorio = new Player("", "UrielVelorio", playerQueries);
        urielVelorio.setPlayerPoints("Street Fighter 6", 1400);
        urielVelorio.setPlayerTier("Street Fighter 6");

        Player squall = new Player("", "Squall", playerQueries);
        squall.setPlayerPoints("Street Fighter 6", 1600);
        squall.setPlayerTier("Street Fighter 6");

        Player inaba = new Player("", "Inaba", playerQueries);
        inaba.setPlayerPoints("Street Fighter 6", 1600);
        inaba.setPlayerTier("Street Fighter 6");

        Player crossoverRD = new Player("", "CrossoverRD", playerQueries);
        crossoverRD.setPlayerPoints("Street Fighter 6", 1600);
        crossoverRD.setPlayerTier("Street Fighter 6");

        Player valmaster = new Player("", "Valmaster", playerQueries);
        valmaster.setPlayerPoints("Street Fighter 6", 1600);
        valmaster.setPlayerTier("Street Fighter 6");

        Player otani = new Player("", "Otani", playerQueries);
        otani.setPlayerPoints("Street Fighter 6", 1600);
        otani.setPlayerTier("Street Fighter 6");

        Player bloo = new Player("", "Bloo", playerQueries);
        bloo.setPlayerPoints("Street Fighter 6", 1600);
        bloo.setPlayerTier("Street Fighter 6");

// Tier 4 - 1400 points
        Player mono = new Player("", "Mono", playerQueries);
        mono.setPlayerPoints("Street Fighter 6", 1400);
        mono.setPlayerTier("Street Fighter 6");

        Player pipoKun = new Player("", "PipoKun", playerQueries);
        pipoKun.setPlayerPoints("Street Fighter 6", 1400);
        pipoKun.setPlayerTier("Street Fighter 6");

        Player nyanpi = new Player("", "Nyanpi", playerQueries);
        nyanpi.setPlayerPoints("Street Fighter 6", 1400);
        nyanpi.setPlayerTier("Street Fighter 6");

        Player ryusei = new Player("", "Ryusei", playerQueries);
        ryusei.setPlayerPoints("Street Fighter 6", 1400);
        ryusei.setPlayerTier("Street Fighter 6");

        Player nishikin = new Player("", "Nishikin", playerQueries);
        nishikin.setPlayerPoints("Street Fighter 6", 1400);
        nishikin.setPlayerTier("Street Fighter 6");

        Player guyGuy = new Player("", "GuyGuy", playerQueries);
        guyGuy.setPlayerPoints("Street Fighter 6", 1400);
        guyGuy.setPlayerTier("Street Fighter 6");

        Player luffy = new Player("", "Luffy", playerQueries);
        luffy.setPlayerPoints("Street Fighter 6", 1400);
        luffy.setPlayerTier("Street Fighter 6");

        Player jabhim = new Player("", "Jabhim", playerQueries);
        jabhim.setPlayerPoints("Street Fighter 6", 1400);
        jabhim.setPlayerTier("Street Fighter 6");

        Player seiya = new Player("", "Seiya", playerQueries);
        seiya.setPlayerPoints("Street Fighter 6", 1400);
        seiya.setPlayerTier("Street Fighter 6");

        Player brianF = new Player("", "Brian F", playerQueries);
        brianF.setPlayerPoints("Street Fighter 6", 1400);
        brianF.setPlayerTier("Street Fighter 6");

        Player motchan = new Player("", "Motchan", playerQueries);
        motchan.setPlayerPoints("Street Fighter 6", 1400);
        motchan.setPlayerTier("Street Fighter 6");

        Player spaceBoy = new Player("", "Space Boy", playerQueries);
        spaceBoy.setPlayerPoints("Street Fighter 6", 1400);
        spaceBoy.setPlayerTier("Street Fighter 6");

        Player kayne = new Player("", "Kayne", playerQueries);
        kayne.setPlayerPoints("Street Fighter 6", 1400);
        kayne.setPlayerTier("Street Fighter 6");

        Player akutagawa = new Player("", "Akutagawa", playerQueries);
        akutagawa.setPlayerPoints("Street Fighter 6", 1400);
        akutagawa.setPlayerTier("Street Fighter 6");

        Player takagi = new Player("", "Takagi", playerQueries);
        takagi.setPlayerPoints("Street Fighter 6", 1400);
        takagi.setPlayerTier("Street Fighter 6");

        Player s4ltyKid = new Player("", "S4ltyKid", playerQueries);
        s4ltyKid.setPlayerPoints("Street Fighter 6", 1400);
        s4ltyKid.setPlayerTier("Street Fighter 6");

        Player kharsonist = new Player("", "Kharsonist", playerQueries);
        kharsonist.setPlayerPoints("Street Fighter 6", 1400);
        kharsonist.setPlayerTier("Street Fighter 6");

        Player adelie = new Player("", "Adelie", playerQueries);
        adelie.setPlayerPoints("Street Fighter 6", 1400);
        adelie.setPlayerTier("Street Fighter 6");

        Player kami = new Player("", "Kami", playerQueries);
        kami.setPlayerPoints("Street Fighter 6", 1400);
        kami.setPlayerTier("Street Fighter 6");

        Player harumi = new Player("", "Harumi", playerQueries);
        harumi.setPlayerPoints("Street Fighter 6", 1400);
        harumi.setPlayerTier("Street Fighter 6");

        Player strider801 = new Player("", "801 Strider", playerQueries);
        strider801.setPlayerPoints("Street Fighter 6", 1400);
        strider801.setPlayerTier("Street Fighter 6");

        Player zabeth = new Player("", "Zabeth", playerQueries);
        zabeth.setPlayerPoints("Street Fighter 6", 1400);
        zabeth.setPlayerTier("Street Fighter 6");

        Player eguto = new Player("", "Eguto", playerQueries);
        eguto.setPlayerPoints("Street Fighter 6", 1400);
        eguto.setPlayerTier("Street Fighter 6");

        Player yangMian = new Player("", "YangMian", playerQueries);
        yangMian.setPlayerPoints("Street Fighter 6", 1400);
        yangMian.setPlayerTier("Street Fighter 6");

        Player snakeEyez = new Player("", "SnakeEyez", playerQueries);
        snakeEyez.setPlayerPoints("Street Fighter 6", 1400);
        snakeEyez.setPlayerTier("Street Fighter 6");

        Player fierce = new Player("", "Fierce", playerQueries);
        fierce.setPlayerPoints("Street Fighter 6", 1400);
        fierce.setPlayerTier("Street Fighter 6");

        Player twoBassa = new Player("", "2BASSA", playerQueries);
        twoBassa.setPlayerPoints("Street Fighter 6", 1400);
        twoBassa.setPlayerTier("Street Fighter 6");

        Player scrawtVermillion = new Player("",  "ScrawtVermillion", playerQueries);
        scrawtVermillion.setPlayerPoints("Street Fighter 6", 1400);
        scrawtVermillion.setPlayerTier("Street Fighter 6");

        Player alphen = new Player("",  "Alphen", playerQueries);
        alphen.setPlayerPoints("Street Fighter 6", 1400);
        alphen.setPlayerTier("Street Fighter 6");

        // Tier 5 - 1200 points
        Player jaccy = new Player("",  "Jaccy", playerQueries);
        jaccy.setPlayerPoints("Street Fighter 6", 1200);
        jaccy.setPlayerTier("Street Fighter 6");

        Player sonicFox = new Player("","SonicFox", playerQueries);
        sonicFox.setPlayerPoints("Street Fighter 6", 1200);
        sonicFox.setPlayerTier("Street Fighter 6");

        Player domaXD = new Player("",  "DomaXD", playerQueries);
        domaXD.setPlayerPoints("Street Fighter 6", 1200);
        domaXD.setPlayerTier("Street Fighter 6");

        Player taisei = new Player("", "Taisei", playerQueries);
        taisei.setPlayerPoints("Street Fighter 6", 1200);
        taisei.setPlayerTier("Street Fighter 6");

        Player hamood = new Player("", "Hamood", playerQueries);
        hamood.setPlayerPoints("Street Fighter 6", 1200);
        hamood.setPlayerTier("Street Fighter 6");

        Player aboodBboy = new Player("",  "Abood bboy", playerQueries);
        aboodBboy.setPlayerPoints("Street Fighter 6", 1200);
        aboodBboy.setPlayerTier("Street Fighter 6");

        Player tako = new Player("",  "Tako", playerQueries);
        tako.setPlayerPoints("Street Fighter 6", 1200);
        tako.setPlayerTier("Street Fighter 6");

        // Tier 1
        playerList.add(punk);
        playerList.add(leshar);
        playerList.add(menaRD);
        playerList.add(xiaohai);
        playerList.add(fuudo);
        playerList.add(kilzyou);
        playerList.add(blaz);
        playerList.add(higuchi);
        playerList.add(yamaguchi);
        playerList.add(kobayan);
        playerList.add(dualKevin);
        playerList.add(bigBird);
        playerList.add(sahara);
        playerList.add(angryBird);

        // Tier 2
        playerList.add(problemX);
        playerList.add(vxbao);
        playerList.add(phenom);
        playerList.add(misterCrimson);
        playerList.add(endingWalker);
        playerList.add(xian);
        playerList.add(kawano);
        playerList.add(itabashiZangief);
        playerList.add(zhen);
        playerList.add(moke);
        playerList.add(go1);
        playerList.add(nl);
        playerList.add(noahTheProdigy);
        playerList.add(chrisWong);
        playerList.add(bonchan);
        playerList.add(shuto);
        playerList.add(kakeru);
        playerList.add(dcq);
        playerList.add(micky);
        playerList.add(momochi);
        playerList.add(kusanagi);
        playerList.add(caba);
        playerList.add(nephew);
        playerList.add(nuckledu);
        playerList.add(ryukichi);
        playerList.add(tokido);
        playerList.add(matsu56);
        playerList.add(oilKing);
        playerList.add(chrisT);
        playerList.add(pugera);
        playerList.add(shine);
        playerList.add(daigo);

        // Tier 3
        playerList.add(bravery);
        playerList.add(bnbbn);
        playerList.add(hotdog29);
        playerList.add(takamura);
        playerList.add(hikaru);
        playerList.add(mochi);
        playerList.add(mago);
        playerList.add(johnTakeuchi);
        playerList.add(haitani);
        playerList.add(craime);
        playerList.add(torimeshi);
        playerList.add(tachikawa);
        playerList.add(juicyJoe);
        playerList.add(idom);
        playerList.add(salvatore);
        playerList.add(yanai);
        playerList.add(elChokotay);
        playerList.add(jak);
        playerList.add(chrisCCH);
        playerList.add(hurricane);
        playerList.add(acqua);
        playerList.add(dakCorgi);
        playerList.add(armperor);
        playerList.add(lexx);
        playerList.add(cosa);
        playerList.add(enzoTheHokage);
        playerList.add(sayff);
        playerList.add(xerna);
        playerList.add(psycho);
        playerList.add(kazunoko);
        playerList.add(broski);
        playerList.add(notPedro);
        playerList.add(riddles);
        playerList.add(hibiki);
        playerList.add(booce);
        playerList.add(nemo);
        playerList.add(kojiKog);
        playerList.add(nyChrisG);
        playerList.add(akira);
        playerList.add(jb);
        playerList.add(joeUmerogan);
        playerList.add(zangiefBolado);
        playerList.add(akainu);
        playerList.add(stealth);
        playerList.add(kincho);
        playerList.add(joey);
        playerList.add(dogura);
        playerList.add(kingsvega);
        playerList.add(urielVelorio);
        playerList.add(squall);
        playerList.add(mizuha);
        playerList.add(inaba);
        playerList.add(crossoverRD);
        playerList.add(valmaster);
        playerList.add(otani);
        playerList.add(bloo);

        // Tier 4
        playerList.add(mono);
        playerList.add(pipoKun);
        playerList.add(nyanpi);
        playerList.add(ryusei);
        playerList.add(nishikin);
        playerList.add(guyGuy);
        playerList.add(luffy);
        playerList.add(jabhim);
        playerList.add(seiya);
        playerList.add(brianF);
        playerList.add(motchan);
        playerList.add(spaceBoy);
        playerList.add(kayne);
        playerList.add(akutagawa);
        playerList.add(camPaine);
        playerList.add(takagi);
        playerList.add(s4ltyKid);
        playerList.add(kharsonist);
        playerList.add(adelie);
        playerList.add(ryanHart);
        playerList.add(diaphone);
        playerList.add(kami);
        playerList.add(harumi);
        playerList.add(strider801);
        playerList.add(zabeth);
        playerList.add(eguto);
        playerList.add(yangMian);
        playerList.add(snakeEyez);
        playerList.add(fierce);
        playerList.add(twoBassa);
        playerList.add(scrawtVermillion);
        playerList.add(alphen);
        playerList.add(fenritti);

        // Tier 5
        playerList.add(jaccy);
        playerList.add(sonicFox);
        playerList.add(domaXD);
        playerList.add(taisei);
        playerList.add(jackieTan);
        playerList.add(hamood);
        playerList.add(kira);
        playerList.add(aboodBboy);
        playerList.add(tako);

        // Tier 6
        playerList.add(dubz);

        // Tier 7
        playerList.add(maltonNEO);
        playerList.add(metalOrca);

        return playerList;
    }

}
