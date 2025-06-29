package MachineLearning;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.api.ndarray.INDArray;


public class Learner {

    private MultiLayerNetwork net;

    public Learner() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
        .seed(123)
        .updater(new Nesterovs(0.01, 0.9))
        .list()
        .layer(new DenseLayer.Builder().nIn(250).nOut(128).activation(Activation.RELU).build())
        .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
            .nIn(128).nOut(5) // 5 cards in hand
            .activation(Activation.SOFTMAX)
            .build())
        .build();

        MultiLayerNetwork net = new MultiLayerNetwork(config);
        net.init();
    }

    public MultiLayerNetwork getNetwork() {
        return net;
    }
}