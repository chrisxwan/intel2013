
/**
 * This class imports the weights learned from the RM6Network class and builds 
 * the Artificial Neural Network corresponding to those weights. It allows users to
 * forecast the salinity at RM6 given input prompts.
 * 
 * @author Christopher Wan 
 */
import java.text.*;
import java.util.*;
import java.io.*;
public class RM6Forecast
{
    final ArrayList<Neuron> inputLayer = new ArrayList<Neuron>();
    final ArrayList<Neuron> hiddenLayer = new ArrayList<Neuron>();
    final ArrayList<Neuron> outputLayer = new ArrayList<Neuron>();
    final Neuron bias = new Neuron();
    final int[] layers;
    final DecimalFormat df;
    final double inputs[][];
    double[][] resultOutputs;
    double[] output;
    final double learningRate = 0.8f;
    final double momentum = 0.1f;
    
    double max1;
    double max2;
    double max3;
    double max4;
    double min1;
    double min2;
    double min3;
    double min4;
    
    public static void main(String[] args) throws IOException
    {
        Scanner in = new Scanner(System.in);
        System.out.println("What is the flow in cubic feet per second? ");
        double q = in.nextDouble();
        System.out.println("What is the rainfall in millimeters?");
        double r = in.nextDouble();
        System.out.println("What is the tide in cubic feet?");
        double t = in.nextDouble();
        RM6Forecast forecast = new RM6Forecast(3, 4, 1, q, r, t, "weightsrm6.txt", "maxminrm6.txt");
        forecast.run();
    }
    
    public RM6Forecast(int input, int hidden, int output, double q, double r, double t, String weight, String maxmin) throws IOException
    {
        int marker = 0;
        
        ArrayList<Double> weights = new ArrayList<Double>();
        ArrayList<Double> mm = new ArrayList<Double>();
        
        Scanner inText4 = new Scanner(new File(weight));
        Scanner inText5 = new Scanner(new File(maxmin));
        

        while(inText4.hasNext())
        {
            weights.add(inText4.nextDouble());
        }
        while(inText5.hasNext())
        {
            mm.add(inText5.nextDouble());
        }
        
        max1 = mm.get(0);
        max2 = mm.get(1);
        max3 = mm.get(2);
        max4 = mm.get(3);
        min1 = mm.get(4);
        min2 = mm.get(5);
        min3 = mm.get(6);
        min4 = mm.get(7);
        
        inputs = new double [1][3];
        resultOutputs = new double[1][1];

        inputs[0][0] = (q - min1) / (max1 - min1);
        inputs[0][1] = (r - min2) / (max2 - min2);
        inputs[0][2] = (t - min3) / (max3 - min3);
        
        
        
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

        for(Neuron neuron : hiddenLayer)
        {
            ArrayList<Connection> connections = neuron.getAllInConnections();
            for(Connection conn : connections)
            {
                double newWeight = weights.get(marker);
                conn.setWeight(newWeight);
                marker++;
            }            
        }
        
        for (Neuron neuron : outputLayer) {
            ArrayList<Connection> connections = neuron.getAllInConnections();
            for (Connection conn : connections) {
                double newWeight = weights.get(marker);
                conn.setWeight(newWeight);
                marker++;
            }
        }

        // reset id counters
        Neuron.counter = 0;
        Connection.counter = 0;
    
    }

    public void setInput(double inputs[])
    {
        for(int i = 0; i < inputLayer.size(); i++)
        {
            inputLayer.get(i).setOutput(inputs[i]);
        }
    }
    
    public double[] getOutput()
    {
        double[] outputs = new double[outputLayer.size()];
        for(int i = 0; i < outputLayer.size(); i++)
        {
            outputs[i] = outputLayer.get(i).getOutput();
        }
        return outputs;
    }
    
    public void activate()
    {
        for(Neuron n : hiddenLayer)
        {
            n.calculateOutput();
        }
        for(Neuron n : outputLayer)
        {
            n.calculateOutput();
        }
    }
    
    public void run()
    {
        for(int p = 0; p < inputs.length; p++)
        {
            setInput(inputs[p]);
            activate();
            
            output = getOutput();
            resultOutputs[p] = output;
        }
        printResult();
    }
    
    public void printResult()
    {
        System.out.println("Salinity Results for RM 6: ");
        for(int p = 0; p < inputs.length; p++)
        {               
            for(int x = 0; x < layers[2]; x++)
            {
                System.out.print((resultOutputs[p][x] * (max4 - min4) + min4) + " psu ");
            }
            System.out.println();
        }
    }
        
}
