package api.requests.steps;

import api.dao.AccountDao;
import api.dao.TransactionDao;
import api.dao.UserDao;
import api.database.Condition;
import api.database.DBRequest;

public class DataBaseSteps {

    public enum DBTables {
        CUSTOMERS("customers"),
        ACCOUNTS("accounts"),
        TRANSACTIONS("transactions"),

        CUSTOMERS_USERNAME("username"),
        ACCOUNTS_ACC_NUM("account_number"),
        ACCOUNTS_ACC_ID("id"),
        TRANSACTIONS_TR_ID("id"),
        TRANSACTIONS_ACC_ID("account_id"),
        TRANSACTIONS_RELATED_ACC_ID("related_account_id");

        DBTables(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }

    public static UserDao getUserByUsername(String username) {
        return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT_AND)
                    .table(DBTables.CUSTOMERS.getName())
                    .where(Condition.equalTo(DBTables.CUSTOMERS_USERNAME.getName(), username))
                    .extractAs(UserDao.class);
    }

    public static AccountDao getAccountByAccountNumber(String accountNumber) {
        return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT_AND)
                    .table(DBTables.ACCOUNTS.getName())
                    .where(Condition.equalTo(DBTables.ACCOUNTS_ACC_NUM.getName(), accountNumber))
                    .extractAs(AccountDao.class);
    }

    public static AccountDao getAccountByAccountId(Integer accountId) {
        return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT_AND)
                    .table(DBTables.ACCOUNTS.getName())
                    .where(Condition.equalTo(DBTables.ACCOUNTS_ACC_ID.getName(), accountId))
                    .extractAs(AccountDao.class);
    }

    public static TransactionDao getTransferByAccountId(Integer accountId) {
        return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT_AND)
                    .table(DBTables.TRANSACTIONS.getName())
                    .where(Condition.equalTo(DBTables.TRANSACTIONS_ACC_ID.getName(), accountId))
                    .extractAs(TransactionDao.class);
    }

    public static TransactionDao getTransferById(Integer id) {
        return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT_AND)
                    .table(DBTables.TRANSACTIONS.getName())
                    .where(Condition.equalTo(DBTables.TRANSACTIONS_TR_ID.getName(), id))
                    .extractAs(TransactionDao.class);
    }

}