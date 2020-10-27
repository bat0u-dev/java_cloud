import com.geekbrains.roganov.common.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    static boolean isRun;

    public static void start() {
        try {
            socket = new Socket("localhost", 8188);
            isRun = true;
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
            isRun = false;
        }
    }

    public static void stop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            isRun = false;
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            isRun = false;
        }
        try {
            socket.close();
            isRun = false;
        } catch (IOException e) {
            e.printStackTrace();
            isRun = false;
        }
    }

    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            isRun = false;
        }
        return false;
    }

    public static AbstractMessage readObject(){
        Object obj = null;
        try {
            obj = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            isRun = false;
        }
        return (AbstractMessage) obj;
    }
}