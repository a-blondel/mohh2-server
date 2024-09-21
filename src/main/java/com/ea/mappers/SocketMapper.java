package com.ea.mappers;

import com.ea.entities.AccountEntity;
import com.ea.entities.LobbyEntity;
import com.ea.utils.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.Instant;

@Mapper(componentModel = "spring", imports = {SocketUtils.class, Timestamp.class, Instant.class})
public abstract class SocketMapper {

    @Autowired
    protected PasswordUtils passwordUtils;

    @BeanMapping(qualifiedByName = "LobbyEntityForCreation")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userHosted", source = "userHosted")
    @Mapping(target = "name", expression = "java(SocketUtils.getValueFromSocket(socket, \"NAME\"))")
    @Mapping(target = "params", expression = "java(SocketUtils.getValueFromSocket(socket, \"PARAMS\"))")
    @Mapping(target = "sysflags", expression = "java(SocketUtils.getValueFromSocket(socket, \"SYSFLAGS\"))")
    @Mapping(target = "pass", expression = "java(SocketUtils.getValueFromSocket(socket, \"PASS\"))")
    @Mapping(target = "minsize", expression = "java(Integer.parseInt(SocketUtils.getValueFromSocket(socket, \"MINSIZE\")))")
    @Mapping(target = "maxsize", expression = "java(Integer.parseInt(SocketUtils.getValueFromSocket(socket, \"MAXSIZE\")))")
    @Mapping(target = "startTime", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "lobbyReports", ignore = true)
    public abstract LobbyEntity toLobbyEntityForCreation(String socket, boolean userHosted);

    @BeanMapping(qualifiedByName = "AccountEntityForCreation")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(SocketUtils.getValueFromSocket(socket, \"NAME\"))")
    @Mapping(target = "pass", expression = "java(SocketUtils.getValueFromSocket(socket, \"PASS\"))")
    @Mapping(target = "loc", expression = "java(SocketUtils.getValueFromSocket(socket, \"LOC\"))")
    @Mapping(target = "mail", expression = "java(SocketUtils.getValueFromSocket(socket, \"MAIL\"))")
    @Mapping(target = "born", expression = "java(SocketUtils.getValueFromSocket(socket, \"BORN\"))")
    @Mapping(target = "zip", expression = "java(SocketUtils.getValueFromSocket(socket, \"ZIP\"))")
    @Mapping(target = "gend", expression = "java(SocketUtils.getValueFromSocket(socket, \"GEND\"))")
    @Mapping(target = "spam", expression = "java(SocketUtils.getValueFromSocket(socket, \"SPAM\"))")
    @Mapping(target = "tos", expression = "java(Integer.parseInt(SocketUtils.getValueFromSocket(socket, \"TOS\")))")
    @Mapping(target = "tick", expression = "java(SocketUtils.getValueFromSocket(socket, \"TICK\"))")
    @Mapping(target = "gamecode", expression = "java(SocketUtils.getValueFromSocket(socket, \"GAMECODE\"))")
    @Mapping(target = "vers", expression = "java(SocketUtils.getValueFromSocket(socket, \"VERS\"))")
    @Mapping(target = "sku", expression = "java(SocketUtils.getValueFromSocket(socket, \"SKU\"))")
    @Mapping(target = "slus", expression = "java(SocketUtils.getValueFromSocket(socket, \"SLUS\"))")
    @Mapping(target = "sdkvers", expression = "java(SocketUtils.getValueFromSocket(socket, \"SDKVERS\"))")
    @Mapping(target = "builddate", expression = "java(SocketUtils.getValueFromSocket(socket, \"BUILDDATE\"))")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "personas", ignore = true)
    public abstract AccountEntity toAccountEntityForCreation(String socket);

    @Named("AccountEntityForCreation")
    @AfterMapping
    public void updateAccountEntityForCreation(@MappingTarget AccountEntity accountEntity) {
        accountEntity.setPass(passwordUtils.bCryptEncode(passwordUtils.ssc2Decode(accountEntity.getPass())));
        accountEntity.setCreatedOn(Timestamp.from(Instant.now()));
    }
    
}
