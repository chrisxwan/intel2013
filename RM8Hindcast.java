
/*
 * This class imports the weights learned from the RM8Network class and builds 
 * the Artificial Neural Network corresponding to those weights. Then, importing existing data
 * for flow, rainfall and tide, this class hindcasts salinity back to 1971.
 * 
 * @author Christopher Wan
 */
import java.text.*;
import java.util.*;
import java.io.*;
public class RM8Hindcast
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
    final double learningRate = 1f;
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
        RM8Hindcast hindcast = new RM8Hindcast(3, 6, 1, "qtest10_temp.txt", "rtest_temp.txt", "ttest_temp.txt", "weights.txt", "maxmin.txt");
        hindcast.run();
    }
    
    public RM8Hindcast(int input, int hidden, int output, String i1, String i2, String i3, String weight, String maxmin) throws IOException
    {
        int marker = 0;
        
        ArrayList<Double> input1 = new ArrayList<Double>();
        ArrayList<Double> input2 = new ArrayList<Double>();
        ArrayList<Double> input3 = new ArrayList<Double>();
        ArrayList<Double> weights = new ArrayList<Double>();
        ArrayList<Double> mm = new ArrayList<Double>();
        
        Scanner inText1 = new Scanner(new File(i1));
        Scanner inText2 = new Scanner(new File(i2));
        Scanner inText3 = new Scanner(new File(i3));
        Scanner inText4 = new Scanner(new File(weight));
        Scanner inText5 = new Scanner(new File(maxmin));
        
        int counter = 0;

        while(inText1.hasNext())
        {
            input1.add(inText1.nextDouble());
            counter++;
        }
        while(inText2.hasNext())
        {
            input2.add(inText2.nextDouble());
        }        
        while(inText3.hasNext())
        {
            input3.add(inText3.nextDouble());
        }
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
        
        inputs = new double [counter][3];
        resultOutputs = new double[counter][1];
                
        for(int x = 0; x < counter; x++)
        {
            inputs[x][0] = (input1.get(x) - min1) / (max1 - min1);
            inputs[x][1] = (input2.get(x) - min2) / (max2 - min2);
            inputs[x][2] = (input3.get(x) - min3) / (max3 - min3);
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
        System.out.println("Hindcast Results for RM 9.1: ");
        for(int p = 0; p < inputs.length; p++)
        {
            for(int x = 0; x < layers[2]; x++)
            {
                System.out.print((resultOutputs[p][x] * (max4 - min4) + min4) + " ");
            }
            System.out.println();
        }
    }
        
}
