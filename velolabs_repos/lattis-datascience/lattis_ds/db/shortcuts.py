# contains the relevant queries for displaying the data
#%%
import traceback
import pandas as pd
from lattis_ds.db.database import MYSQL, main_mysql_credentials
import numpy as np
import datetime


def extract_data_from_steps(s):
    # steps are strings, convert to obtain start,end lats
    steps = s.replace('[', '')
    steps = steps.replace(']', '')
    steps = steps.split(',')
    start_lat, start_lng, start_ts, end_lat, end_lng, end_ts = None, None, None, None, None, None
    if len(steps) > 0:
        start_lat = steps[0]
    if len(steps) > 1:
        start_lng = steps[1]  
    if len(steps) > 2:
        start_ts = steps[2]
    if len(steps) >= 6:
        end_lat = steps[-3]
        end_lng = steps[-2] 
        end_ts = steps[-1]
    return dict(
        start_lat=start_lat,
        start_lng=start_lng,
        start_ts=start_ts, 
        end_lat=end_lat, 
        end_lng=end_lng, 
        end_ts=end_ts)


def get_timestamps(dstart, dend, period='daily'):
    tstart = int(datetime.datetime.timestamp(dstart))
    tend = int(datetime.datetime.timestamp(dend))
    if period == 'daily':
        return list(range(tstart, tend, 24 * 3600)) + [tend]
    elif period == 'hourly':
        return list(range(tstart, tend, 3600)) + [tend]


class Shortcuts:
    def __init__(self, mysql):
        self.mysql = mysql

    def get_steps_trips(self, fleet_id, year, month, day):
        """ query to get start location of trips by day. 
        User can select by hour on the frontend. """

        query = f"""
            SELECT trip_id, 
                from_unixtime(date_created) as nice_date,
                fleet_id,
                steps 
            FROM lattis_main.trips
            WHERE YEAR(from_unixtime(date_created)) = {year}
                AND MONTH(from_unixtime(date_created)) = {month}
                AND DAY(from_unixtime(date_created)) = {day}
                AND fleet_id = {fleet_id}

        """
        return self.mysql.query(query)

    def get_start_end_trips(self, fleet_id, year, month, day):
        print(f"Query for {year}, {month}, {day} for {fleet_id}")
        df = self.get_steps_trips(fleet_id, year, month, day)
        dfnice = df.steps.apply(extract_data_from_steps).apply(pd.Series)
        dfnice = dfnice.astype(float)
        if len(dfnice) == 0:
            return None
        dfnice['trip_id'] = df.trip_id
        dfnice['start_ts'] = pd.to_datetime(dfnice.start_ts * 1e9)
        dfnice['start_hour'] = dfnice.start_ts.apply(lambda x: x.hour)
        dfnice['end_ts'] = pd.to_datetime(dfnice.end_ts * 1e9)
        dfnice['end_hour'] = dfnice.end_ts.apply(lambda x: x.hour)
        #print(dfnice)
        return dfnice

    def get_bike_history(self, fleet_id, start_ts, end_ts):
        # Select the last timestamp in the db and all rows that are in between
        query = f"""SELECT bsh.* 
            FROM bikes_status_history bsh 
            WHERE bsh.timestamp = (SELECT MAX(bsh2.timestamp)
                        FROM bikes_status_history bsh2  
                        WHERE bsh.bike_id = bsh2.bike_id  
                        AND bsh2.timestamp <= {start_ts})
            OR (
                        bsh.timestamp > {start_ts} 
                        AND bsh.timestamp < {end_ts})
            AND fleet_id = {fleet_id}
 	
            ORDER by bsh.bike_id, bsh.timestamp """

        # query is ordered by bike and timestamp
        df = self.mysql.query(query)
        return df

    def get_active_vehicles_per_period(self, fleet_id, start_date, end_date, period='daily'):
        daily_timestamps = get_timestamps(start_date, end_date, period=period)
        df = self.get_bike_history(fleet_id, daily_timestamps[0], daily_timestamps[-1])
        daily_status = []
        for t in daily_timestamps:
            assert(type(t) == int)
            # for each of the timestamp obtain the last status by bike id 
            # and count the different number of each status   
            value_count = df[df.timestamp <= t].groupby('bike_id').status.last().value_counts().to_dict()
            value_count['timestamp'] = t
            daily_status.append(value_count)
        print(daily_status)
        return pd.DataFrame(daily_status)

    def get_trips_data(self, fleet_id, t_start, t_end):
        query  = f"""
            SELECT t.bike_id,
                t.fleet_id,
                t.user_id,
                t.trip_id,
                t.date_created, 
                t.duration,
                tpt.total,
                tpt.transaction_id IS NULL as valid, 
                t.duration <= 7200 AND NOT (tpt.transaction_id IS NULL) as under_2_hours 
            FROM lattis_main.trips t
            LEFT JOIN trip_payment_transactions tpt
            ON t.trip_id = tpt.trip_id
            WHERE date_created >= {t_start}
            AND date_created < {t_end}
            AND t.fleet_id = {fleet_id}"""
        return self.mysql.query(query)

    def get_hourly_data(self, fleet_id, start_date, end_date):
        return self.get_agg_trips_data(fleet_id, start_date, end_date, period='hourly')

    def get_daily_data(self, fleet_id, start_date, end_date):
        return self.get_agg_trips_data(fleet_id, start_date, end_date, period='daily')

    def get_agg_trips_data(self, fleet_id, start_date, end_date, period='daily'):
        """ obtain a dataframe with agregates between start and end date
        period can be 'daily' or 'hourly' """
        assert (period in ("daily", "hourly"))
        timestamps = get_timestamps(start_date, end_date, period=period)
        df = self.get_trips_data(fleet_id, timestamps[0], timestamps[-1])
        if period == 'daily':
            df['date'] = pd.to_datetime(df.date_created * 1e9).apply(lambda x: x.date())
        else:
            df['date'] = pd.to_datetime(df.date_created * 1e9).apply(lambda x: (x.date(), x.hour))
        df['duration'] = df.duration.fillna(0.)
        # index by date only dates where we have data
        daily_df = self.get_daily_vehicles_with_at_least_a_ride(df)
        gby = df.groupby(['date'])
        valid_gby = df[df.valid==1].groupby(['date'])
        valid_under_2_gby = df[df.under_2_hours==1].groupby(['date']) 
        daily_df['n_trips'] = gby.bike_id.count()
        daily_df['n_valid'] = gby.valid.count()
        daily_df['n_valid_under_2_hours'] = gby.under_2_hours.count()
        daily_df['gross_revenue'] = valid_gby.total.sum()
        daily_df['gross_revenue_under_2_hours'] = valid_under_2_gby.total.sum()
        daily_df['n_unique_users'] = gby.user_id.nunique()
        
        try:
            daily_df['avg_duration'] = gby.duration.mean()
            daily_df['avg_duration_valid'] = valid_gby.duration.mean()
            daily_df['avg_duration_valid_under_2'] = valid_under_2_gby.duration.mean()
            daily_df['avg_valid_trip_ticket'] = daily_df.gross_revenue / daily_df.n_valid
            daily_df['avg_trips_per_users'] = (daily_df.n_valid / daily_df.n_unique_users).fillna(0.)
        
        except:
            traceback.print_exc()
            print("not enough data")
        for k in daily_df.columns:
            if k.startswith('avg'):
                daily_df[k].fillna(0.)

        actives_df = self.get_active_vehicles_per_period(fleet_id, start_date, end_date, period=period)
        if period == 'daily':
            actives_df['date'] = pd.to_datetime(actives_df.timestamp * 1e9).apply(lambda x: x.date())
        else:
            actives_df['date'] = pd.to_datetime(actives_df.timestamp * 1e9).apply(lambda x: (x.date(), x.hour))

        cols = ['active', 'inactive', 'suspended', 'deleted']
        for c in cols:
            if c not in actives_df.columns:
                actives_df[c] = 0.
        actives_df = actives_df.groupby('date')['active', 'inactive', 'suspended', 'deleted'].first()

        daily_df = daily_df.join(actives_df, how='outer')
        daily_df['utilization'] = daily_df.n_trips / daily_df.active
        daily_df['activation'] = daily_df.n_with_a_trip / daily_df.active
        #daily_df.utilization = daily_df.utilization.fillna(0.)
        #daily_df.activation = daily_df.activation.fillna(0.)
        daily_df = daily_df.fillna(0.)
        # in some case buggy data could lead to a 500 -> inf -> na
        daily_df = daily_df.replace([np.inf, -np.inf], np.nan).fillna('')
        return daily_df.reset_index()

    def get_daily_vehicles_with_at_least_a_ride(self, df):
        """ group by the date and count unique bikes """
        return pd.DataFrame(df.groupby(['date']).bike_id.nunique().rename('n_with_a_trip'))

    def get_fleet_id_names(self):

        query = """ SELECT fleet_id, fleet_name FROM lattis_main.fleets """
    
        df = self.mysql.query(query)
        return df.set_index('fleet_id')['fleet_name'].to_dict()



# from lattis_ds.db.database import MYSQL, main_mysql_credentials
# mysql = MYSQL(credentials=main_mysql_credentials)
# sh = Shortcuts(mysql)

# df = sh.get_fleet_id_names()
