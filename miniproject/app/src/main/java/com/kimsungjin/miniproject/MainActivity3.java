package com.kimsungjin.miniproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.os.Bundle;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity3 extends AppCompatActivity {
    int InputSize = 8;
    String model_name = "inference_model.tflite";
    String quantized_model_name = "quantized_inference_model_int8.tflite";
    MappedByteBuffer tfliteModel;
    Interpreter segmentationDNN;
    Interpreter.Options options = new Interpreter.Options();
    ImageView imageView;
    int[][] imageView_id = new int[9][9];
    public int[][] imagetype2;
    public int turn;
    int PV_EVALUATE_COUNT;
    TextView textView;
    long inference_time_64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        imageView_id = id_array();
        imagetype2 = ((MainActivity4)MainActivity4.context_main).imagetype;
        turn = ((MainActivity4)MainActivity4.context_main).chose_label_idx;
        EditText editText = ((MainActivity4)MainActivity4.context_main).findViewById(R.id.treesearchnum);
        PV_EVALUATE_COUNT = Integer.valueOf(editText.getText().toString());
        int[] pieces = new int[64];
        int[] enemy_pieces = new int[64];

        if(turn == R.id.black){
            for(int i=1; i<9; i++){
                for(int j=1; j<9; j++){
                    imageView = findViewById(imageView_id[i][j]);
                    if(imagetype2[i][j] == 0){
                        imageView.setImageResource(R.drawable.black);
                        pieces[(i-1)*8 + j-1] = 1;
                    }else if(imagetype2[i][j] == 1){
                        imageView.setImageResource(R.drawable.white);
                        enemy_pieces[(i-1)*8 + j-1] = 1;
                    }else{
                        imageView.setImageResource(R.drawable.none);
                    }
                }
            }
        }else{
            for(int i=1; i<9; i++){
                for(int j=1; j<9; j++){
                    imageView = findViewById(imageView_id[i][j]);
                    if(imagetype2[i][j] == 0){
                        imageView.setImageResource(R.drawable.black);
                        enemy_pieces[(i-1)*8 + j-1] = 1;
                    }else if(imagetype2[i][j] == 1){
                        imageView.setImageResource(R.drawable.white);
                        pieces[(i-1)*8 + j-1] = 1;
                    }else{
                        imageView.setImageResource(R.drawable.none);
                    }
                }
            }
        }
        State state = new State(pieces, enemy_pieces, 0);
        if(state.legal_actions().get(0) != 64){
            for(int i=0; i<state.legal_actions().size(); i++){
                imageView = findViewById(imageView_id[state.legal_actions().get(i) / 8 + 1][state.legal_actions().get(i) % 8 + 1]);
                imageView.setImageResource(R.drawable.available);
            }
            try{
                //tfliteModel = loadModelFile(this, model_name);
                tfliteModel = loadModelFile(this, quantized_model_name);
            }catch(Exception e){
            }
            segmentationDNN = new Interpreter(tfliteModel, options);

            float[][][][] input_data = new float[1][InputSize][InputSize][2];
            Map<Integer, Object> output_data = new HashMap<>();
            float[][] output1 = new float[1][1];
            float[][] output2 = new float[1][65];
            output_data.put(0, output1);
            output_data.put(1, output2);
            for (int i = 0; i < state.pieces.length; i++) {
                input_data[0][i / 8][i % 8][0] = state.pieces[i];
                input_data[0][i / 8][i % 8][1] = state.enemy_pieces[i];
            }
            Object[] inputArray = {input_data};
            long beforeTime = System.currentTimeMillis();
            segmentationDNN.runForMultipleInputsOutputs(inputArray, output_data);
            long afterTime = System.currentTimeMillis();
            inference_time_64 = (afterTime - beforeTime);
            float value = ((float[][]) output_data.get(0))[0][0];
            textView = findViewById(R.id.boardvalue);
            textView.setText("Board_Value : " + Float.toString(value));

            ArrayList<Integer> scores = pv_mcts_action(state);
            int max = 0;
            int max_idx = 0;
            for(int i=0; i<scores.size(); i++){
                if(i==0){
                    max = scores.get(0);
                }else if(max < scores.get(i)){
                    max = scores.get(i);
                    max_idx = i;
                }else if(max == scores.get(i)){
                    if(Math.random() > 0.5){
                        max = scores.get(i);
                        max_idx = i;
                    }
                }
            }
            int best_choice = state.legal_actions().get(max_idx);

            if(best_choice != 64){
                imageView = findViewById(imageView_id[best_choice / 8 + 1][best_choice % 8 + 1]);
                imageView.setImageResource(R.drawable.best);
                textView = findViewById(R.id.bestchoice);
                textView.setText("Best Choice : " + Integer.toString(best_choice / 8 + 1) + Integer.toString(best_choice % 8 + 1));

                for(int i=0; i<state.legal_actions().size(); i++){
                    LinearLayout linearLayout = findViewById(R.id.linearLayout3);
                    TextView newtext = new TextView(MainActivity3.this);
                    newtext.setText("n_value of Choice " + Integer.toString(state.legal_actions().get(i) / 8 + 1) + Integer.toString(state.legal_actions().get(i) % 8 + 1) + " : " + Integer.toString(scores.get(i)));
                    newtext.setTextSize(2, 15);
                    newtext.setTypeface(newtext.getTypeface(), Typeface.BOLD);
                    linearLayout.addView(newtext);
                }
            }
        }
        Toast.makeText(getApplicationContext(), "One inference took " + Long.toString(inference_time_64) + "ms", Toast.LENGTH_SHORT).show();
    }

    public int[][] id_array(){
        int[][] id = new int[9][9];
        id[1][1] = R.id.b11;
        id[1][2] = R.id.b12;
        id[1][3] = R.id.b13;
        id[1][4] = R.id.b14;
        id[1][5] = R.id.b15;
        id[1][6] = R.id.b16;
        id[1][7] = R.id.b17;
        id[1][8] = R.id.b18;

        id[2][1] = R.id.b21;
        id[2][2] = R.id.b22;
        id[2][3] = R.id.b23;
        id[2][4] = R.id.b24;
        id[2][5] = R.id.b25;
        id[2][6] = R.id.b26;
        id[2][7] = R.id.b27;
        id[2][8] = R.id.b28;

        id[3][1] = R.id.b31;
        id[3][2] = R.id.b32;
        id[3][3] = R.id.b33;
        id[3][4] = R.id.b34;
        id[3][5] = R.id.b35;
        id[3][6] = R.id.b36;
        id[3][7] = R.id.b37;
        id[3][8] = R.id.b38;

        id[4][1] = R.id.b41;
        id[4][2] = R.id.b42;
        id[4][3] = R.id.b43;
        id[4][4] = R.id.b44;
        id[4][5] = R.id.b45;
        id[4][6] = R.id.b46;
        id[4][7] = R.id.b47;
        id[4][8] = R.id.b48;

        id[5][1] = R.id.b51;
        id[5][2] = R.id.b52;
        id[5][3] = R.id.b53;
        id[5][4] = R.id.b54;
        id[5][5] = R.id.b55;
        id[5][6] = R.id.b56;
        id[5][7] = R.id.b57;
        id[5][8] = R.id.b58;

        id[6][1] = R.id.b61;
        id[6][2] = R.id.b62;
        id[6][3] = R.id.b63;
        id[6][4] = R.id.b64;
        id[6][5] = R.id.b65;
        id[6][6] = R.id.b66;
        id[6][7] = R.id.b67;
        id[6][8] = R.id.b68;

        id[7][1] = R.id.b71;
        id[7][2] = R.id.b72;
        id[7][3] = R.id.b73;
        id[7][4] = R.id.b74;
        id[7][5] = R.id.b75;
        id[7][6] = R.id.b76;
        id[7][7] = R.id.b77;
        id[7][8] = R.id.b78;

        id[8][1] = R.id.b81;
        id[8][2] = R.id.b82;
        id[8][3] = R.id.b83;
        id[8][4] = R.id.b84;
        id[8][5] = R.id.b85;
        id[8][6] = R.id.b86;
        id[8][7] = R.id.b87;
        id[8][8] = R.id.b88;

        return id;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String model_name) throws IOException{
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(model_name);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public class State{
        private int[][] dxy = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
        private boolean pass_end = false;
        private int[] pieces;
        private int[] enemy_pieces;
        private int depth;

        public State(int[] pieces, int[] enemy_pieces, int depth){
            this.pieces = pieces;
            this.enemy_pieces = enemy_pieces;
            this.depth = depth;
        }

        public int piece_count(int[] pieces){
            int count = 0;
            for(int i=0; i<pieces.length; i++){
                if(pieces[i] == 1){
                    count += 1;
                }
            }
            return count;
        }

        public boolean is_done(){
            return (this.piece_count(this.pieces) + this.piece_count(this.enemy_pieces) == 64) || this.pass_end;
        }

        public boolean is_lose(){
            return this.is_done() && (this.piece_count(this.pieces) < this.piece_count(this.enemy_pieces));
        }

        public boolean is_win(){
            return this.is_done() && (this.piece_count(this.pieces) > this.piece_count(this.enemy_pieces));
        }

        public boolean is_draw(){
            return this.is_done() && (this.piece_count(this.pieces) == this.piece_count(this.enemy_pieces));
        }

        public State next(int action){
            State state = new State(this.pieces.clone(), this.enemy_pieces.clone(), this.depth + 1);
            if(action != 64){
                state.is_legal_action_xy(action % 8, action / 8, true);
            }
            int[] w = state.pieces;
            state.pieces = state.enemy_pieces;
            state.enemy_pieces = w;

            if(action == 64 && state.legal_actions() == new ArrayList<>(Arrays.asList(64))){
                state.pass_end = true;
            }
            return state;
        }

        public boolean is_legal_action_xy_dxy(int xx, int yy, int dx, int dy, boolean flip){
            xx = xx + dx;
            yy = yy + dy;
            if(yy < 0 || 7 < yy || xx < 0 || 7 < xx || (this.enemy_pieces[xx + yy*8] != 1)){
                return false;
            }
            for(int j=0; j<8; j++){
                if(yy < 0 || 7 < yy || xx < 0 || 7 < xx || (this.enemy_pieces[xx + yy*8] == 0 && this.pieces[xx + yy*8] == 0)){
                    return false;
                }
                if(this.pieces[xx + yy*8] == 1){
                    if(flip){
                        for(int i=0; i<8; i++){
                            xx = xx - dx;
                            yy = yy - dy;
                            if(this.pieces[xx + yy*8] == 1){
                                return true;
                            }
                            this.pieces[xx + yy*8] = 1;
                            this.enemy_pieces[xx + yy*8] = 0;
                        }
                    }
                    return true;
                }
                xx = xx + dx;
                yy = yy + dy;
            }
            return false;
        }

        public boolean is_legal_action_xy(int x, int y, boolean flip){
            if(this.enemy_pieces[x + y*8] == 1 || this.pieces[x + y*8] == 1){
                return false;
            }
            if(flip){
                this.pieces[x + y*8] = 1;
            }
            boolean flag = false;
            for(int i=0; i<8; i++){
                int ddx = this.dxy[i][0];
                int ddy = this.dxy[i][1];
                if(this.is_legal_action_xy_dxy(x, y, ddx, ddy, flip)){
                    flag = true;
                }
            }
            return flag;
        }

        public ArrayList<Integer> legal_actions(){
            ArrayList<Integer> actions = new ArrayList<>();
            for(int j=0; j<8; j++){
                for(int i=0; i<8; i++){
                    if(this.is_legal_action_xy(i, j, false)){
                        actions.add(i + j*8);
                    }
                }
            }
            if(actions.size() == 0){
                actions.add(64);
            }
            return actions;
        }
    }

    public class Node {
        private State state;
        private float p;
        private float w = 0;
        private int n = 0;
        private ArrayList<Node> child_nodes = null;

        public Node(State state, float p) {
            this.state = state;
            this.p = p;
        }

        public float evaluate() {
            float value = 0;
            if (this.state.is_done()) {
                if (this.state.is_lose()) {
                    value = -1;
                }
                if (this.state.is_win()) {
                    value = 1;
                }
                if (this.state.is_draw()) {
                    value = 0;
                }

                this.w += value;
                this.n += 1;
                return value;
            }

            if (this.child_nodes == null) {
                float[][][][] input_data = new float[1][InputSize][InputSize][2];
                Map<Integer, Object> output_data = new HashMap<>();
                float[][] output1 = new float[1][1];
                float[][] output2 = new float[1][65];
                output_data.put(0, output1);
                output_data.put(1, output2);
                for (int i = 0; i < this.state.pieces.length; i++) {
                    input_data[0][i / 8][i % 8][0] = this.state.pieces[i];
                    input_data[0][i / 8][i % 8][1] = this.state.enemy_pieces[i];
                }
                Object[] inputArray = {input_data};
                segmentationDNN.runForMultipleInputsOutputs(inputArray, output_data);
                float[][] pol = (float[][]) output_data.get(1);
                value = ((float[][]) output_data.get(0))[0][0];
                ArrayList<Float> policies = new ArrayList<>();
                ArrayList<Integer> leg_actions = this.state.legal_actions();
                float sum = 0;
                for (int i = 0; i < leg_actions.size(); i++) {
                    policies.add(pol[0][leg_actions.get(i)]);
                    sum += pol[0][leg_actions.get(i)];
                }
                if (policies.size() != 0) {
                    for (int i = 0; i < policies.size(); i++) {
                        policies.set(i, policies.get(i) / sum);
                    }
                }

                this.w += value;
                this.n += 1;
                this.child_nodes = new ArrayList<>();
                for (int i = 0; i < leg_actions.size(); i++) {
                    int action = leg_actions.get(i);
                    float policy = policies.get(i);
                    this.child_nodes.add(new Node(this.state.next(action), policy));
                }
                return value;
            } else {
                value = -1 * (this.next_child_node().evaluate());
                this.w = value;
                this.n += 1;
                return value;
            }
        }

        public Node next_child_node() {
            float C_PUCT = 1.0f;
            double t = 0;
            ArrayList<Integer> child_nodes_score = nodes_to_scores(this.child_nodes);
            for (int i = 0; i < child_nodes_score.size(); i++) {
                t += child_nodes_score.get(i);
            }
            float max = 0;
            int max_idx = 0;
            for (int i = 0; i < this.child_nodes.size(); i++) {
                Node child_node = this.child_nodes.get(i);
                float pucb_value = 0;
                if (child_node.n != 0) {
                    pucb_value += (-1 * child_node.w / child_node.n);
                }
                pucb_value += (C_PUCT * child_node.p * (float) Math.sqrt(t) / (1 + child_node.n));
                if (i == 0) {
                    max = pucb_value;
                } else if (max < pucb_value) {
                    max = pucb_value;
                    max_idx = i;
                } else if (max == pucb_value){
                    if(Math.random() > 0.5){
                        max = pucb_value;
                        max_idx = i;
                    }
                }
            }
            return this.child_nodes.get(max_idx);
        }
    }

    public ArrayList<Integer> nodes_to_scores(ArrayList<Node> nodes){
            ArrayList<Integer> scores = new ArrayList<>();
            for(int i=0; i<nodes.size(); i++){
                scores.add(nodes.get(i).n);
            }
            return scores;
    }

    public ArrayList<Integer> pv_mcts_action(State state){
        Node root_node = new Node(state, 0);
        for(int i=0; i<PV_EVALUATE_COUNT; i++){
            root_node.evaluate();
        }
        return nodes_to_scores(root_node.child_nodes);
    }
}