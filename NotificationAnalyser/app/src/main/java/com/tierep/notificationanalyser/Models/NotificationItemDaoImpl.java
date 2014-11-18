package com.tierep.notificationanalyser.models;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import com.tierep.notificationanalyser.NotificationAppView;
import com.tierep.notificationanalyser.NotificationDateView;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * NotificationItem DAO implementation.
 *
 * Created by pieter on 24/09/14.
 */
public class NotificationItemDaoImpl extends BaseDaoImpl<NotificationItem, Integer> implements NotificationItemDao {
    public NotificationItemDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, NotificationItem.class);
    }

    @Override
    public List<NotificationAppView> getOverviewToday() throws SQLException {
        return this.getOverviewDay(new Date());
    }

    @Override
    public List<NotificationAppView> getOverviewDay(Date date) throws SQLException {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = df.format(date);

        String rawQuery = "SELECT " + NotificationItem.FIELD_PACKAGE_NAME
                + ", COUNT(*) FROM " + NotificationItem.FIELD_TABLE_NAME
                + " WHERE strftime('%d-%m-%Y'," + NotificationItem.FIELD_DATE + ") = '" + dateString + "'"
                    + " AND " + NotificationItem.FIELD_PACKAGE_NAME + " IN "
                        + " (SELECT  " + Application.FIELD_PACKAGE_NAME
                        + " FROM " + Application.FIELD_TABLE_NAME
                        + " WHERE " + Application.FIELD_IGNORE + " = 0)"
                + " GROUP BY " + NotificationItem.FIELD_PACKAGE_NAME;

        return this.getOverviewGeneric(rawQuery);
    }

    @Override
    public List<NotificationAppView> getOverviewWeek(Date date) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date firstDayOfWeek = cal.getTime();

        cal.add(Calendar.DATE, 6);
        Date lastDayOfWeek = cal.getTime();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateStringFirstDay = df.format(firstDayOfWeek);
        String dateStringLastDay = df.format(lastDayOfWeek);

        String rawQuery = "SELECT " + NotificationItem.FIELD_PACKAGE_NAME
                + ", COUNT(*) FROM " + NotificationItem.FIELD_TABLE_NAME
                + " WHERE strftime('%Y-%m-%d'," + NotificationItem.FIELD_DATE + ") >= '" + dateStringFirstDay + "'"
                + " AND strftime('%Y-%m-%d'," + NotificationItem.FIELD_DATE + ") <= '" + dateStringLastDay + "'"
                + " AND " + NotificationItem.FIELD_PACKAGE_NAME + " IN "
                + " (SELECT  " + Application.FIELD_PACKAGE_NAME
                + " FROM " + Application.FIELD_TABLE_NAME
                + " WHERE " + Application.FIELD_IGNORE + " = 0)"
                + " GROUP BY " + NotificationItem.FIELD_PACKAGE_NAME;

        return this.getOverviewGeneric(rawQuery);
    }

    @Override
    public List<NotificationAppView> getOverviewMonth(Date date) throws SQLException {
        DateFormat df = new SimpleDateFormat("MM-yyyy");
        String dateString = df.format(date);

        String rawQuery = "SELECT " + NotificationItem.FIELD_PACKAGE_NAME
                + ", COUNT(*) FROM " + NotificationItem.FIELD_TABLE_NAME
                + " WHERE strftime('%m-%Y'," + NotificationItem.FIELD_DATE + ") = '" + dateString + "'"
                + " AND " + NotificationItem.FIELD_PACKAGE_NAME + " IN "
                + " (SELECT  " + Application.FIELD_PACKAGE_NAME
                + " FROM " + Application.FIELD_TABLE_NAME
                + " WHERE " + Application.FIELD_IGNORE + " = 0)"
                + " GROUP BY " + NotificationItem.FIELD_PACKAGE_NAME;

        return this.getOverviewGeneric(rawQuery);
    }


    private List<NotificationAppView> getOverviewGeneric(String rawQuery) throws SQLException {
        List<NotificationAppView> list = new LinkedList<NotificationAppView>();
        int maxCount = 0;

        GenericRawResults<String[]> rawResults = this.queryRaw(rawQuery);
        List<String[]> results = rawResults.getResults();

        for (String[] result : results) {
            int ntfCount = Integer.parseInt(result[1]);
            maxCount = ntfCount > maxCount ? ntfCount : maxCount;
        }
        for (String[] result : results) {
            list.add(new NotificationAppView(result[0], Integer.parseInt(result[1]), maxCount));
        }
        return list;
    }

    @Override
    public List<NotificationDateView> getSummaryLastDays(int days) throws SQLException {
        String rawQuery = "SELECT " + NotificationItem.FIELD_DATE
                        + ", COUNT(*) FROM " + NotificationItem.FIELD_TABLE_NAME
                        + " WHERE " + NotificationItem.FIELD_PACKAGE_NAME + " IN "
                            + " (SELECT  " + Application.FIELD_PACKAGE_NAME
                            + " FROM " + Application.FIELD_TABLE_NAME
                            + " WHERE " + Application.FIELD_IGNORE + " = 0)"
                        + " GROUP BY strftime('%d-%m-%Y', " + NotificationItem.FIELD_DATE + ")"
                        + " ORDER BY datetime(" + NotificationItem.FIELD_DATE + ") DESC "
                        + " LIMIT " + days;
        return this.getSummaryLastPeriod(rawQuery);
    }

    @Override
    public List<NotificationDateView> getSummaryLastWeeks(int weeks) throws SQLException {
        String rawQuery = "SELECT " + NotificationItem.FIELD_DATE
                + ", COUNT(*) FROM " + NotificationItem.FIELD_TABLE_NAME
                + " WHERE " + NotificationItem.FIELD_PACKAGE_NAME + " IN "
                + " (SELECT  " + Application.FIELD_PACKAGE_NAME
                + " FROM " + Application.FIELD_TABLE_NAME
                + " WHERE " + Application.FIELD_IGNORE + " = 0)"
                + " GROUP BY strftime('%W-%Y', " + NotificationItem.FIELD_DATE + ")"
                + " ORDER BY datetime(" + NotificationItem.FIELD_DATE + ") DESC "
                + " LIMIT " + weeks;

        return this.getSummaryLastPeriod(rawQuery);
    }

    @Override
    public List<NotificationDateView> getSummaryLastMonths(int months) throws SQLException {
        String rawQuery = "SELECT " + NotificationItem.FIELD_DATE
                + ", COUNT(*) FROM " + NotificationItem.FIELD_TABLE_NAME
                + " WHERE " + NotificationItem.FIELD_PACKAGE_NAME + " IN "
                + " (SELECT  " + Application.FIELD_PACKAGE_NAME
                + " FROM " + Application.FIELD_TABLE_NAME
                + " WHERE " + Application.FIELD_IGNORE + " = 0)"
                + " GROUP BY strftime('%m-%Y', " + NotificationItem.FIELD_DATE + ")"
                + " ORDER BY datetime(" + NotificationItem.FIELD_DATE + ") DESC "
                + " LIMIT " + months;

        return this.getSummaryLastPeriod(rawQuery);
    }

    private List<NotificationDateView> getSummaryLastPeriod(String rawQuery) throws SQLException {
        LinkedList<NotificationDateView> list = new LinkedList<NotificationDateView>();
        GenericRawResults<String[]> rawResults = this.queryRaw(rawQuery);
        List<String[]> results = rawResults.getResults();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        for (String[] result : results) {
            try {
                Date date = formatter.parse(result[0]);
                Integer notifications = Integer.parseInt(result[1]);
                list.add(new NotificationDateView(date, notifications));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        Collections.reverse(list);
        return list;
    }
}
