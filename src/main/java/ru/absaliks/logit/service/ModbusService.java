package ru.absaliks.logit.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import ru.absaliks.logit.ModbusMasterBuilder;
import ru.absaliks.logit.config.Config;
import ru.absaliks.logit.model.ArchiveEntry;
import ru.absaliks.logit.model.JournalEntry;
import ru.absaliks.logit.model.ServicePage;
import ru.absaliks.logit.tasks.ReadArchiveTask;
import ru.absaliks.logit.tasks.ReadJournalTask;
import ru.absaliks.logit.tasks.ReadServicePageTask;

public class ModbusService {

  private static final Logger LOG = Logger.getLogger(ModbusService.class.getName());

  private static ModbusService instance;
  private ServicePage servicePage;

  private ModbusService() {
  }

  public static ModbusService getInstance() {
    if (instance == null) {
      instance = new ModbusService();
    }
    return instance;
  }

  public void checkConnection() throws Exception {
    AbstractModbusMaster master = null;
    try {
      master = getConnection();
    } finally {
      if (nonNull(master)) {
        master.disconnect();
      }
    }
  }

  public ServicePage getServicePage() {
    return servicePage;
  }

  public ServicePage readServicePage() throws Exception {
    LOG.info("Reading service page");
    AbstractModbusMaster master = null;
    try {
      master = getConnection();
      this.servicePage = new ReadServicePageTask(master, getSlaveId()).call();
      return this.servicePage;
    } finally {
      disconnect(master);
    }
  }

  private void disconnect(AbstractModbusMaster master) {
    LOG.log(Level.INFO, "Closing connection");
    if (master != null)
      master.disconnect();
  }

  public List<JournalEntry> readJournal(DoubleProperty progress) throws Exception {
    LOG.info("Reading journal...");
    checkServicePage();
    AbstractModbusMaster master = null;
    try {
      master = getConnection();
      return new ReadJournalTask(master, getSlaveId(), servicePage.journalPageCount, progress)
          .call();
    } finally {
      disconnect(master);
    }
  }

  private void checkServicePage() throws Exception {
    if (isNull(servicePage)) {
      LOG.info("Service page is null, going to read it first");
      readServicePage();
    }
  }

  public List<ArchiveEntry> readArchive(DoubleProperty progress)
      throws Exception {
    checkServicePage();
    AbstractModbusMaster master = null;
    try {
      master = getConnection();
      return new ReadArchiveTask(master, getSlaveId(), servicePage.archivePageCount, progress)
          .call();
    } finally {
      disconnect(master);
    }
  }

  private int getSlaveId() {
    return Config.getInstance().modbus.deviceId == 0 ? 1 : Config.getInstance().modbus.deviceId;
  }

  private AbstractModbusMaster getConnection() throws Exception {
    LOG.config("Opening connection");
    AbstractModbusMaster master = ModbusMasterBuilder.createMaster();
    master.connect();
    return master;
  }

  public Set<String> getRegisteredCOMPorts() {
    return new SerialConnection().getCommPorts();
  }
}
