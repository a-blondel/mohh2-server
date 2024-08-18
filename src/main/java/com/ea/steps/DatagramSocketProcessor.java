package com.ea.steps;

import com.ea.dto.DatagramSocketData;
import com.ea.entities.LobbyReportEntity;
import com.ea.repositories.LobbyReportRepository;
import com.ea.utils.BeanUtil;
import com.ea.utils.SocketUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static com.ea.utils.SocketUtils.parseHexString;
import static com.ea.utils.SocketUtils.formatIntToWord;

@Slf4j
public class DatagramSocketProcessor {

    public static final int RAW_PACKET_INIT = 1;
    public static final int RAW_PACKET_CONN = 2;
    public static final int RAW_PACKET_DISC = 3;
    public static final int RAW_PACKET_POKE = 5;
    public static final int GAME_PACKET_USER_UNRELIABLE = 7;
    public static final int GAME_PACKET_USER_UNRELIABLE_AND_GAME_PACKET_SYNC = 71; // 7 + 64
    public static final int GAME_PACKET_SYNC = 64;
    public static final int RAW_PACKET_DATA = 256;
    public static final int RAW_PACKET_UNREL = 128;
    private static LobbyReportRepository lobbyReportRepository = BeanUtil.getBean(LobbyReportRepository.class);

    /**
     * Prepares the output message based on request type,
     * then calls the writer
     * @param socket the socket to give to the writer
     * @param socketData the object to process
     */
    public static void process(DatagramSocket socket, DatagramSocketData socketData) {

        DatagramPacket inputPacket = socketData.getInputPacket();
        byte[] buf = Arrays.copyOf(inputPacket.getData(), inputPacket.getLength());

        int packetSeq = new BigInteger(1, buf, 0, 4).intValue();

        if (RAW_PACKET_POKE == packetSeq) {
            System.arraycopy(parseHexString(formatIntToWord(RAW_PACKET_CONN)), 0, buf, 0, 4);
//        if (RAW_PACKET_POKE == packetSeq) {
//            System.arraycopy(parseHexString(formatIntToWord(RAW_PACKET_INIT)), 0, buf, 0, 4);
//        } else if (RAW_PACKET_INIT == packetSeq) {
//            System.arraycopy(parseHexString(formatIntToWord(RAW_PACKET_CONN)), 0, buf, 0, 4);
        } else if (RAW_PACKET_DISC == packetSeq) {
            // FIXME : There can be multiple lobby reports for the same IP (most likely an error case not correctly handled)
            Optional<LobbyReportEntity> lobbyReportEntityOpt =
                    lobbyReportRepository.findCurrentLobbyReportByIP(
                            SocketUtils.handleLocalhostIp(inputPacket.getAddress().getHostAddress()));
            if(lobbyReportEntityOpt.isPresent()) {
                LobbyReportEntity lobbyReportEntity = lobbyReportEntityOpt.get();
                lobbyReportEntity.setEndTime(Timestamp.from(Instant.now()));
                lobbyReportRepository.save(lobbyReportEntity);
            }
        } else if (RAW_PACKET_UNREL <= packetSeq && RAW_PACKET_DATA > packetSeq) {
            int packetOperation = new BigInteger(1, buf, inputPacket.getLength() - 1, 1).intValue();
            if (GAME_PACKET_USER_UNRELIABLE == packetOperation) {
                // 0000009c 00000109 00000000 21421344 1d000000 1c000000 fcff1f00 e0ff1f00 e0ff1f00 e0ff1f04 00 07

                // 0000009c = packetSeq
                // 00000109 = ack of RAW_PACKET_DATA
                // 00000000
                // 21421344 = checksum of 1d000000 1c000000 fcff1f00 e0ff1f00 e0ff1f00 e0ff1f04 00
                // 1d000000 = increment from 1 in little endian ?
                // 1c000000 = ack of increment -1 in little endian ?
                // fcff1f 00 = ?
                // e0ff1f 00 = almost identical to the previous one ?
                // e0ff1f 00 = identical to the previous one ?
                // e0ff1f 0400 = identical to the previous one + ?
                // 07 = packet type (GAME_PACKET_USER_UNRELIABLE)
            } else if (GAME_PACKET_USER_UNRELIABLE_AND_GAME_PACKET_SYNC == packetOperation)  {
                // 0000009d 00000109 00000000 8b5fc3e7 1e000000 1d000000 fcff3f00 c0ff3f00 c0ff3f00 c0ff3f04 00
                // 41c4b77f 41c4b7bc 0054 47

                // 0000009d = packetSeq
                // 00000109 = ack of previous RAW_PACKET_DATA
                // 00000000
                // 8b5fc3e7 = checksum of 1e000000 1d000000 fcff3f 00 c0ff3f00 c0ff3f00 c0ff3f04 00
                // 1e000000 = increment from 1 in little endian ?
                // 1d000000 = ack of increment -1 in little endian ?
                // fcff3f 00 = ?
                // c0ff3f 00 = almost identical to the previous one ?
                // c0ff3f 00 = identical to the previous one
                // c0ff3f 0400 = identical to the previous one  + ?

                // 41c4b77f = last tick
                // 41c4b7bc = current tick
                // 0054 = game latency
                // 47 = packet type (GAME_PACKET_USER_UNRELIABLE (7) + GAME_PACKET_SYNC (40))
            } else if (GAME_PACKET_SYNC == packetOperation)  {
                // 0000136b 0000136b 113a34cb 113a3563 0098 40

                // 0000136b = packetSeq
                // 0000136b = ack of previous RAW_PACKET_DATA
                // 113a34cb = last tick
                // 113a3563 = current tick
                // 0098 = game latency
                // 40 = packet type (GAME_PACKET_SYNC (40))
            }
        } else if (RAW_PACKET_DATA <= packetSeq) {
            // Nothing to do yet...
        }

        socketData.setOutputMessage(buf);

        DatagramSocketWriter.write(socket, socketData);

    }

}
