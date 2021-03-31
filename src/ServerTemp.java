import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ServerTemp implements Runnable {
    Selector selector;
    ServerSocketChannel serverSocketChannel;
    InetSocketAddress inetSocketAddress;
    private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to NioChat!\n".getBytes());
    private final int port = 2222;

    ServerTemp() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        inetSocketAddress = new InetSocketAddress(port);
        serverSocketChannel.bind(inetSocketAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {

        try {
            System.out.println("Server start | port: " + port);

            while (true) {
                selector.select();
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                SelectionKey key;
                while (selectionKeyIterator.hasNext()) {
                    key = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (key.isAcceptable()) this.accept(key);
                    if (key.isReadable()) this.read(key);
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    private void accept(SelectionKey key) throws IOException {
        //возвращает СокетЧанел
        //accept блокирует до тех пор пока не будет получено соединение
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = (new StringBuilder(socketChannel.socket().getInetAddress().toString()))
                .append(":")
                .append(socketChannel.socket().getPort()).toString();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, address);
        socketChannel.write(welcomeBuf);
        welcomeBuf.rewind();
        System.out.println("Connect client: " + address);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(140);
        socketChannel.read(byteBuffer);
        String result = (new StringBuilder().append(key.attachment())
                .append(": ")
                .append(new String(byteBuffer.array()).trim())
                .append("\n")).toString();
        System.out.println(result);
        echo(result);
    }

    private void echo(String message) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel sch = (SocketChannel) key.channel();
                sch.write(byteBuffer);
                byteBuffer.rewind();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerTemp server = new ServerTemp();
        new Thread(server).start();
    }


}
