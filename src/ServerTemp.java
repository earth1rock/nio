import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ServerTemp {
    public static void main(String[] args) throws IOException {

        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        InetSocketAddress inetSocketAddress = new InetSocketAddress(2222);

        serverSocketChannel.bind(inetSocketAddress);

        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {

            /*
            When you call select() or selectNow() on the Selector
            it gives you only the SelectableChannel instances that actually has data to read
             */
            selector.select();

            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

            while (selectionKeyIterator.hasNext()) {
                SelectionKey key = selectionKeyIterator.next();
                System.out.println("temp");
                if (key.isAcceptable()) {
                    System.out.println("Connect client");

                    //возвращает СокетЧанел
                    //accept блокирует до тех пор пока не будет получено соединение
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);

                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(140);
                    socketChannel.read(byteBuffer);
                    String result = new String(byteBuffer.array()).trim();
                    System.out.println(result);
                }
                selectionKeyIterator.remove();
            }

        }
    }
}
