/*
 * RM9_1Network is the artificial neural network that learns the salinity
 * process for River Mile 9.1 in the Loxahatchee Network.
 * 
 * @author Christopher Wan
 * 
 */

import java.text.*;
import java.util.*;
import java.io.*;



public class RM9_1Network {
    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    final boolean isTrained = false;
    final DecimalFormat df;
    final Random rand = new Random();
    final ArrayList<Neuron> inputLayer = new ArrayList<Neuron>();
    final ArrayList<Neuron> hiddenLayer = new ArrayList<Neuron>();
    final ArrayList<Neuron> outputLayer = new ArrayList<Neuron>();
    final Neuron bias = new Neuron();
    final int[] layers;
    final int randomWeightMultiplier = 1;
    PrintWriter result = new PrintWriter(new FileWriter("results.txt"));

    final double epsilon = 0.000000001;

    final double learningRate = 0.8f; //set the learning rate
    final double momentum = 0.1f; //set the momentum

    final double inputs[][];

    // Corresponding outputs, xor training data
    final double expectedOutputs[][];
    double resultOutputs[][]; // dummy init
    double output[];
    
    double max1;
    double max2;
    double max3;
    double max4;
    double min1;
    double min2;
    double min3;
    double min4;

    // for weight update all
    final HashMap<String, Double> weightUpdate = new HashMap<String, Double>();

    public static void main(String[] args) throws IOException
    {
        int numberInputs = 3; //set the number of input neurons
        int numberHidden = 6; //set the number of hidden neurons
        int numberOutputs = 1; //set the number of output neurons
        RM9_1Network nn = new RM9_1Network(numberInputs, numberHidden, numberOutputs, "qtrain10.txt", "rtrain.txt", "trealtrain.txt", "RM9.1train.txt");
        int maxRuns = 50000;
        double minErrorCondition = 0.001;
        nn.run(maxRuns, minErrorCondition);
    }

    public RM9_1Network(int input, int hidden, int output, String i1, String i2, String i3, String o1) throws IOException
    {
        PrintWriter outFile = new PrintWriter(new FileWriter("maxminRM9_1.txt"));
        
        ArrayList<Double> input1 = new ArrayList<Double>();
        ArrayList<Double> input2 = new ArrayList<Double>();
        ArrayList<Double> input3 = new ArrayList<Double>();
        ArrayList<Double> output1 = new ArrayList<Double>();
        
        Scanner inText1 = new Scanner(new File(i1));
        Scanner inText2 = new Scanner(new File(i2));
        Scanner inText3 = new Scanner(new File(i3));
        Scanner inText6 = new Scanner(new File(o1));
        
        int counter = 0;
        max1 = Integer.MIN_VALUE;
        max2 = Integer.MIN_VALUE;
        max3 = Integer.MIN_VALUE;
        max4 = Integer.MIN_VALUE;
        min1 = Integer.MAX_VALUE;
        min2 = Integer.MAX_VALUE;
        min3 = Integer.MAX_VALUE;
        min4 = Integer.MAX_VALUE;
        while(inText1.hasNext())
        {
            input1.add(inText1.nextDouble());
        }
        while(inText2.hasNext())
        {
            input2.add(inText2.nextDouble());
        }
        while(inText3.hasNext())
        {
            input3.add(inText3.nextDouble());
        }
        while(inText6.hasNext())
        {
            output1.add(inText6.nextDouble());        
            counter++;
        }
        for(int x = 0; x < input1.size(); x++)
        {
            if(input1.get(x) > max1)
            {
                max1 = input1.get(x);
            }            
            if(input2.get(x) > max2)
            {
                max2 = input2.get(x);
            }
            if(input3.get(x) > max3)
            {
                max3 = input3.get(x);
            }
            if(output1.get(x) > max4)
            {
                max4 = output1.get(x);
            }
            if(input1.get(x) < min1)
            {
                min1 = input1.get(x);
            }
            if(input2.get(x) < min2)
            {
                min2 = input2.get(x);
            }
            if(input3.get(x) < min3)
            {
                min3 = input3.get(x);
            }
            if(output1.get(x) < min4)
            {
                min4 = output1.get(x);
            }
        }

        inputs = new double [counter][3];
        expectedOutputs = new double[counter][1];
        resultOutputs = new double[counter][1];
        
        
        
        for(int x = 0; x < counter; x++)
        {
            inputs[x][0] = (input1.get(x) - min1) / (max1 - min1);
            inputs[x][1] = (input2.get(x) - min2) / (max2 - min2);
            inputs[x][2] = (input3.get(x) - min3) / (max3 - min3);
            expectedOutputs[x][0] = (output1.get(x) - min4) / (max4 - min4);
            resultOutputs[x][0] = -1.0;
        }
                     
        this.layers = new int[] { input, hidden, output };
        df = new DecimalFormat("#.0#");

        /**
         * Create all neurons and connections Connections are created in the
         * neuron class
         */
        for (int i = 0; i < layers.length; i++) {
            if (i == 0) { // input layer
                for (int j = 0; j < layers[i]; j++) {
                    Neuron neuron = new Neuron();
                    inputLayer.add(neuron);
                }
            } else if (i == 1) { // hidden layer
                for (int j = 0; j < layers[i]; j++) {
                    Neuron neuron = new Neuron();
                    neuron.addInConnectionsS(inputLayer);
                    neuron.addBiasConnection(bias);
                    hiddenLayer.add(neuron);
                }
            }

            else if (i == 2) { // output layer
                for (int j = 0; j < layers[i]; j++) {
                    Neuron neuron = new Neuron();
                    neuron.addInConnectionsS(hiddenLayer);
                    neuron.addBiasConnection(bias);
                    outputLayer.add(neuron);
                }
            } else {
                System.out.println("!Error NeuralNetwork init");
            }
        }

        // initialize random weights
        for (Neuron neuron : hiddenLayer) {
            ArrayList<Connection> connections = neuron.getAllInConnections();
            for (Connection conn : connections) {
                double newWeight = getRandom();
                conn.setWeight(newWeight);
            }
        }
        for (Neuron neuron : outputLayer) {
            ArrayList<Connection> connections = neuron.getAllInConnections();
            for (Connection conn : connections) {
                double newWeight = getRandom();
                conn.setWeight(newWeight);
            }
        }
        
        outFile.println(max1);
        outFile.println(max2);
        outFile.println(max3);
        outFile.println(max4);
        outFile.println(min1);
        outFile.println(min2);
        outFile.println(min3);
        outFile.println(min4);
        outFile.close();
        
        // reset id counters
        Neuron.counter = 0;
        Connection.counter = 0;

    }

    // random
    double getRandom() {
        return randomWeightMultiplier * (rand.nextDouble() * 2 - 1); // [-1;1[
    }

    /**
     * 
     * @param inputs
     *            There is equally many neurons in the input layer as there are
     *            in input variables
     */
    public void setInput(double inputs[]) {
        for (int i = 0; i < inputLayer.size(); i++) {
            inputLayer.get(i).setOutput(inputs[i]);
        }
    }

    public double[] getOutput() {
        double[] outputs = new double[outputLayer.size()];
        for (int i = 0; i < outputLayer.size(); i++)
            outputs[i] = outputLayer.get(i).getOutput();
        return outputs;
    }

    /**
     * Calculate the output of the neural network based on the input The forward
     * operation
     */
    public void activate() {
        for (Neuron n : hiddenLayer)
            n.calculateOutput();
        for (Neuron n : outputLayer)
            n.calculateOutput();
    }

    /**
     * all output propagate back
     * 
     * @param expectedOutput
     *            first calculate the partial derivative of the error with
     *            respect to each of the weight leading into the output neurons
     *            bias is also updated here
     */
    public void applyBackpropagation(double expectedOutput[]) {

        // error check, normalize value ]0;1[
        for (int i = 0; i < expectedOutput.length; i++) {
            double d = expectedOutput[i];
            if (d < 0 || d > 1) {
                if (d < 0)
                    expectedOutput[i] = 0 + epsilon;
                else
                    expectedOutput[i] = 1 - epsilon;
            }
        }

        int i = 0;
        for (Neuron n : outputLayer) {
            ArrayList<Connection> connections = n.getAllInConnections();
            for (Connection con : connections) {
                double ak = n.getOutput();
                double ai = con.leftNeuron.getOutput();
                double desiredOutput = expectedOutput[i];

                double partialDerivative = -ak * (1 - ak) * ai
                        * (desiredOutput - ak);
                double deltaWeight = -learningRate * partialDerivative;
                double newWeight = con.getWeight() + deltaWeight;
                con.setDeltaWeight(deltaWeight);
                con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
            }
            i++;
        }

        // update weights for the hidden layer
        for (Neuron n : hiddenLayer) {
            ArrayList<Connection> connections = n.getAllInConnections();
            for (Connection con : connections) {
                double aj = n.getOutput();
                double ai = con.leftNeuron.getOutput();
                double sumKoutputs = 0;
                int j = 0;
                for (Neuron out_neu : outputLayer) {
                    double wjk = out_neu.getConnection(n.id).getWeight();
                    double desiredOutput = (double) expectedOutput[j];
                    double ak = out_neu.getOutput();
                    j++;
                    sumKoutputs = sumKoutputs
                            + (-(desiredOutput - ak) * ak * (1 - ak) * wjk);
                }

                double partialDerivative = aj * (1 - aj) * ai * sumKoutputs;
                double deltaWeight = -learningRate * partialDerivative;
                double newWeight = con.getWeight() + deltaWeight;
                con.setDeltaWeight(deltaWeight);
                con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
            }
        }
    }

    void run(int maxSteps, double minError) throws IOException {
        int i;
        int counter = 0;
        // Train neural network until minError reached or maxSteps exceeded
        double error = 1;
        double globalError = 0;
        for (i = 0; i < maxSteps && error > minError; i++) {
            error = 0;
            for (int p = 0; p < inputs.length; p++) {
                setInput(inputs[p]);

                activate();

                output = getOutput();
                resultOutputs[p] = output;
                
                if(i == maxSteps - 1)
                {
                    for (int j = 0; j < expectedOutputs[p].length; j++)
                    {
                        double err = (Math.pow(output[j] - expectedOutputs[p][j], 2));
                        globalError += err;
                        counter++;
                    }
                }
                for (int j = 0; j < expectedOutputs[p].length; j++) {
                    double err = (Math.pow(output[j] - expectedOutputs[p][j], 2));
                    error += err;
                }

                applyBackpropagation(expectedOutputs[p]);
            }
        }
        
        globalError /= counter;
        globalError = Math.sqrt(globalError);
        printResult();
        
        System.out.println("Root Mean Squared Error = " + globalError);
        System.out.println("##### EPOCH " + i+"\n");
        printAllWeights();
    }
    
    void printResult()
    {
        System.out.println("RM 9.1 Training Results: ");
        for (int p = 0; p < inputs.length; p++) {
            for (int x = 0; x < layers[2]; x++) {
                System.out.print((resultOutputs[p][x] * (max4 - min4) + min4));
            }
            System.out.println();
        }
        System.out.println();
    }
    

    public void printAllWeights() throws IOException {
        PrintWriter outFile = new PrintWriter(new FileWriter("weightsRM9_1.txt"));        
        System.out.println("printAllWeights");
        // weights for the hidden layer
        for (Neuron n : hiddenLayer) {
            ArrayList<Connection> connections = n.getAllInConnections();
            for (Connection con : connections) {
                double w = con.getWeight();
                System.out.println("n=" + n.id + " c=" + con.id + " w=" + w);
                outFile.println(w);
            }
        }
        // weights for the output layer
        for (Neuron n : outputLayer) {
            ArrayList<Connection> connections = n.getAllInConnections();
            for (Connection con : connections) {
                double w = con.getWeight();
                System.out.println("n=" + n.id + " c=" + con.id + " w=" + w);
                outFile.println(w);
            }
        }
        System.out.println();
        outFile.close();
        

    }
}