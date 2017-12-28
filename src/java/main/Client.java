package main;

import scr.Action;
import scr.Controller;
import scr.MessageBasedSensorModel;
import scr.SocketHandler;

import java.util.StringTokenizer;


/**
 * @author Daniele Loiacono
 */
public class Client {

    private static int UDP_TIMEOUT = 1000;
    private static int port;
    private static String host;
    private static String clientId;
    private static boolean verbose;
    private static int maxEpisodes;
    private static int maxSteps;
    private static int ticks = 0;
    private static int raceCounter = 0;

    /**
     * @param args is used to define all the options of the client.
     *             <port:N> is used to specify the port for the connection (default is 3001)
     *             <host:ADDRESS> is used to specify the address of the host where the server is running (default is localhost)
     *             <id:ClientID> is used to specify the ID of the client sent to the server (default is championship2009)
     *             <verbose:on> is used to set verbose mode on (default is off)
     *             <maxEpisodes:N> is used to set the number of episodes (default is 1)
     *             <maxSteps:N> is used to set the max number of steps for each episode (0 is default value, that means unlimited number of steps)
     *             <stage:N> is used to set the current stage: 0 is WARMUP, 1 is QUALIFYING, 2 is RACE, others value means UNKNOWN (default is UNKNOWN)
     *             <trackName:name> is used to set the name of current track
     */
    public static void main(String[] args) {
        parseParameters(args);
        runClient(args);
    }

    public static boolean runClient(String[] args) {
        SocketHandler mySocket = new SocketHandler(host, port, verbose);
        String inMsg;

        int syncRepair = 0;

        Controller driver = load();

        /* Build init string */
        float[] angles = driver.initAngles();

        String initStr = clientId + "(init";
        for (int i = 0; i < angles.length; i++) {
            initStr = initStr + " " + angles[i];
        }
        initStr = initStr + ")";
        Action action = new Action();

        long curEpisode = 0;
        boolean shutdownOccurred = false;

        do {

            /*
             * Client identification
             */
            do {
                mySocket.send(initStr);
                inMsg = mySocket.receive(UDP_TIMEOUT);
            } while (inMsg == null || inMsg.indexOf("***identified***") < 0);



            /*
             * Start to drive
             */
            long currStep = 0;
            while (true) {

                /*
                 * Receives from TORCS the game state
                 */
                inMsg = mySocket.receive(UDP_TIMEOUT);
                long t = System.nanoTime();
                if (inMsg != null) {

                    /*
                     * Check if race is ended (shutdown)
                     */
                    if (inMsg.indexOf("***shutdown***") >= 0) {
                        System.out.println("Shutting down!");
                        shutdownOccurred = true;
                        break;
                    }

                    /*
                     * Check if race is restarted
                     */
                    if (inMsg.indexOf("***restart***") >= 0) {
                        driver.reset();
                        if (verbose)
                            System.out.println("Server restarting!");
                        break;
                    }

                    if (currStep < maxSteps || maxSteps == 0) {
                        Client.ticks++;

                        action = driver.control(new MessageBasedSensorModel(inMsg));

                    } else
                        action.restartRace = true;

                    currStep++;
                    t = System.nanoTime() - t;
                    if (t / 1000000.0 >= 5.0) {
                        syncRepair++;
                    }
                    if (syncRepair > 0) {//This is to be sure that we are in sync with server
                        syncRepair--;
                        continue;
                    }

                    if (currStep % 300 == 0) {//this is a delay to be sure if we are not sync with the server and we didnt detect that, we restart ourself every 1000 ticks
                        syncRepair++;
                    }

                    mySocket.send(action.toString());
                } else {
                }

            }
        } while (++curEpisode < maxEpisodes && !shutdownOccurred);

        /*
         * Shutdown the controller
         */
        driver.shutdown();
        mySocket.close();
        return true;
    }

    private static void parseParameters(String[] args) {
        /*
         * Set default values for the options
         */
        port = 3001;
        host = "localhost";
        clientId = "SCR";
        verbose = false;
        maxEpisodes = 1;
        maxSteps = 0;

        for (int i = 1; i < args.length; i++) {
            StringTokenizer st = new StringTokenizer(args[i], ":");
            String entity = st.nextToken();
            String value = st.nextToken();
            if (entity.equals("--port")) {
                port = Integer.parseInt(value);
            }

            if (entity.equals("maxEpisodes")) {
                maxEpisodes = Integer.parseInt(value);
                if (maxEpisodes <= 0) {
                    System.out.println(entity + ":" + value
                            + " is not a valid option");
                    System.exit(0);
                }
            }
        }
    }

    private static Controller load() {
        return new DefaultDriver();
    }
}
