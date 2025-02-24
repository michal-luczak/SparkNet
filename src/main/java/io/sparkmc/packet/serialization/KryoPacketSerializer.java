package io.sparkmc.packet.serialization;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

@RequiredArgsConstructor
public class KryoPacketSerializer implements PacketSerializer {

    private final Kryo kryo;

    @Override
    public <T extends Serializable> byte[] serialize(T packet) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Output output = new Output(byteStream);
        kryo.writeClassAndObject(output, packet);
        output.close();
        return byteStream.toByteArray();
    }

    @Override
    public Object deserialize(byte[] bytes) {
        return kryo.readClassAndObject(new Input(bytes));
    }
}
