package com.ea.services;

import com.ea.dto.SessionData;
import com.ea.dto.SocketData;
import com.ea.entities.AccountEntity;
import com.ea.mappers.SocketMapper;
import com.ea.repositories.AccountRepository;
import com.ea.steps.SocketWriter;
import com.ea.utils.AccountUtils;
import com.ea.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ea.utils.SocketUtils.getValueFromSocket;

@Component
public class AccountService {

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private SocketMapper socketMapper;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Account creation
     * @param socket
     * @param socketData
     */
    public void acct(Socket socket, SocketData socketData) {
        String name = getValueFromSocket(socketData.getInputMessage(), "NAME");

        Optional<AccountEntity> accountEntityOpt = accountRepository.findByName(name);
        if (accountEntityOpt.isPresent()) {
            socketData.setIdMessage("acctdupl"); // Duplicate account error (EC_DUPLICATE)
            int alts = Integer.parseInt(getValueFromSocket(socketData.getInputMessage(), "ALTS"));
            if (alts > 0) {
                String opts = AccountUtils.suggestNames(alts, name);
                Map<String, String> content = Stream.of(new String[][]{
                        { "OPTS", opts }
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
                socketData.setOutputData(content);
            }
        } else {
            AccountEntity accountEntity = socketMapper.toAccountEntityForCreation(socketData.getInputMessage());
            accountRepository.save(accountEntity);
        }
        SocketWriter.write(socket, socketData);
    }

    /**
     * Account update
     * @param socket
     * @param socketData
     */
    public void edit(Socket socket, SocketData socketData) {
        String name = getValueFromSocket(socketData.getInputMessage(), "NAME");

        Optional<AccountEntity> accountEntityOpt = accountRepository.findByName(name);
        if (accountEntityOpt.isPresent()) {
            AccountEntity accountEntity = accountEntityOpt.get();

            String pass = getValueFromSocket(socketData.getInputMessage(), "PASS");
            String mail = getValueFromSocket(socketData.getInputMessage(), "MAIL");
            String spam = getValueFromSocket(socketData.getInputMessage(), "SPAM");
            String chng = getValueFromSocket(socketData.getInputMessage(), "CHNG");

            boolean update = false;
            boolean error = false;

            if (mail != null && !mail.equals(accountEntity.getMail())) {
                accountEntity.setMail(mail);
                update = true;
            }

            if (!pass.equals(chng)) {
                if (passwordUtils.bCryptMatches(pass, accountEntity.getPass())) {
                    accountEntity.setPass(passwordUtils.bCryptEncode(chng));
                    update = true;
                } else {
                    socketData.setIdMessage("editpass"); // Invalid password error (EC_INV_PASS)
                    error = true;
                }
            }

            if (!error && (update || !spam.equals(accountEntity.getSpam()))) {
                accountEntity.setSpam(spam);
                accountEntity.setUpdatedOn(Timestamp.from(Instant.now()));
                accountRepository.save(accountEntity);
            }

        } else {
            socketData.setIdMessage("editimst"); // Inexisting error (EC_INV_MASTER)
        }

        SocketWriter.write(socket, socketData);
    }

    /**
     * Account login
     * @param socket
     * @param socketData
     */
    public void auth(Socket socket, SessionData sessionData, SocketData socketData) {
        String name = getValueFromSocket(socketData.getInputMessage(), "NAME");
        String pass = getValueFromSocket(socketData.getInputMessage(), "PASS");

        Optional<AccountEntity> accountEntityOpt = accountRepository.findByName(name);
        if (accountEntityOpt.isPresent()) {
            AccountEntity accountEntity = accountEntityOpt.get();
            String decodedPass = passwordUtils.ssc2Decode(pass);
            if (passwordUtils.bCryptMatches(decodedPass, accountEntity.getPass())) {
                sessionData.setCurrentAccount(accountEntity);

                String personas = accountEntity.getPersonas().stream()
                        .filter(p -> p.getDeletedOn() == null)
                        .map(p -> p.getPers())
                        .collect(Collectors.joining(","));
                Map<String, String> content = Stream.of(new String[][]{
                        { "NAME", accountEntity.getName() },
                        { "ADDR", socket.getInetAddress().getHostAddress() },
                        { "PERSONAS", "" + personas },
                        { "LOC", "" + accountEntity.getLoc() },
                        { "MAIL", accountEntity.getMail() },
                        { "SPAM", accountEntity.getSpam() },
                        { "BORN", accountEntity.getBorn() },
                        { "TOS", "1" },
                        { "GEND", "M" },
                        { "FROM", "US" },
                        { "LANG", "en" },
                        { "_LUID", "$000000000b32588d" },
                        { "MID", "0000000000000000000000000000000000000000000000000000000000000000" },
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
                socketData.setOutputData(content);
            } else {
                socketData.setIdMessage("authpass"); // Invalid password error (EC_INV_PASS)
            }
        } else {
            socketData.setIdMessage("authimst"); // Inexisting error (EC_INV_MASTER)
        }

        SocketWriter.write(socket, socketData);
    }


    /**
     * Lost username or password
     * If we receive 'MAIL', then we have to send the username matching (if present)
     * If we receive 'NAME', then we have to send an email to reset the password of the account liked to the username (if present)
     */
    public void lost(Socket socket, SocketData socketData) {

    }

    public void usld(Socket socket, SessionData sessionData, SocketData socketData) {

        Map<String, String> content = Stream.of(new String[][]{
                { "IMGATE", "0" },
                { "SPM_EA", "0" },
                { "SPM_PART", "0" },
                { "QMSG0", "Wanna play?" },
                { "QMSG1", "I rule!" },
                { "QMSG2", "Doh!" },
                { "QMSG3", "Mmmm... doughnuts." },
                { "QMSG4", "What time is it?" },
                { "QMSG5", "The truth is out of style." },
                { "UID", "$000000000b32588d" },
                { "MID", "0000000000000000000000000000000000000000000000000000000000000000" },
                //{ "PROMO_RESPONSE", "N" },
                //{ "PROMO_TEXT", "Test" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        socketData.setOutputData(content);

        SocketWriter.write(socket, socketData);
    }

}
