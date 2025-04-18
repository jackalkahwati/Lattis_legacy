import dash_core_components as dcc
import dash_html_components as html
import geohash
import pandas as pd
import plotly.express as px
import datetime
import dash_bootstrap_components as dbc
import traceback

from datetime import date

MAPBOX_TK = 'pk.eyJ1IjoiamVyZW15cmljYXJkIiwiYSI6ImNrMTk4dnJtdjF6aDczcHRrZ250bTh0YmoifQ.nOLWdq-JtlSl62lncP6IpQ'


"""
Note Jeremy 
Vehicles
Duration graph in minutes
Date range by default month to day (if day > 5)
Change column names to be Item name

"""

def gh_encode(row):
    return geohash.encode(row.start_lat, row.start_lng, precision=6)


def gh_encode_end(row):
    return geohash.encode(row.end_lat, row.end_lng, precision=6)


class HeatMap:
    def __init__(self, fleet_id, sh, year, month, day, is_start, is_agg):
        self.sh = sh
        self.fleet_id = fleet_id
        self.set_date(year, month, day)
        self.is_start = is_start
        self.is_agg = is_agg
        self.get_data()
    
    def set_start(self):
        self.is_start = True
    
    def set_agg(self, is_agg):
        self.is_agg = is_agg

    def set_end(self):
        self.is_start = False
    
    def set_date(self, year, month, day):
        self.year = year
        self.month = month
        self.day = day

    def get_data(self):
        try:
            trips = self.sh.get_start_end_trips(fleet_id=self.fleet_id, year=self.year, month=self.month, day=self.day)
            trips = trips[trips.start_lat != 0]
            trips = trips[trips.end_lat != 0]
            trips = trips[trips.start_lng != 0]
            trips = trips[trips.end_lng != 0]

            trips['gh_start'] = trips.apply(gh_encode, axis=1)
            trips['gh_end'] = trips.apply(gh_encode_end, axis=1)
            print("trips")
            print(trips)
        except :
            trips = None
            print(f"NO trips for {(self.year, self.month, self.day)} ")
            traceback.print_exc()

        self.trips = trips
        self.current_date = (self.year, self.month, self.day)

    def get_scatter(self):
        if self.trips is None:
            return html.Div("No Data")
        gby_cols  = []
        
        animation_frame = None
        if self.is_start:
            gby_cols.append('gh_start')
        else:
            gby_cols.append('gh_end')

        if not self.is_agg:
            if self.is_start:
                gby_cols.append('start_hour')
                animation_frame = 'start_hour'
            else:
                gby_cols.append('end_hour')
                animation_frame = 'end_hour'
    
        scatter = self.trips.groupby(gby_cols)['trip_id'].count()
        scatter = pd.DataFrame(scatter).reset_index()
        scatter['n_ride'] = scatter.trip_id
        scatter.drop('trip_id', axis=1)

        if self.is_start:
            scatter['lat'], scatter['lng'] = zip(*scatter.gh_start.map(geohash.decode))
        else:
            scatter['lat'], scatter['lng'] = zip(*scatter.gh_end.map(geohash.decode))

        scatter = scatter.sort_values(gby_cols[-1])

        print(scatter)
        px.set_mapbox_access_token(MAPBOX_TK)
        fig = px.scatter_mapbox(scatter, lat='lat', lon='lng', color='trip_id', size='n_ride', animation_frame=animation_frame,
            color_continuous_scale='inferno')
        fig.update_layout(mapbox_style="light")

        return dcc.Graph(figure=fig, style = {'display': 'inline-block', 'width': '88%', 'height': '800px'})   


def get_heat_map(sh, fleet_id, year=None, month=None, day=None, is_start=True, is_agg=False):
    if year is None:
        now = date.today()
        year = now.year
        month = now.month
        day = now.day

    return  html.Div(
            children=[
                dbc.Row([
                    dcc.DatePickerSingle(
                        id='heatmap_date',
                        min_date_allowed=date(1995, 8, 5),
                        max_date_allowed=datetime.datetime.now().date(),
                        date=date.today(),
                        style = {'display': 'inline-block', 'width': '20%'}
                    ),
                    dcc.RadioItems(
                        id='hour_selection',
                        options=[{'label': 'Daily aggregated', 'value': 'aggregated'}, {'label': 'Hourly animation', 'value': 'animation'}],
                        value='animation',
                        style = {'display': 'inline-block', 'width': '20%'}
                        ),
                    dcc.RadioItems(
                        id='start_end_selection',
                        options=[{'label': ' Display start of trips ', 'value': 'start'}, {'label': 'Display end of Trips', 'value': 'end'}],
                        value='start',
                        style = {'display': 'inline-block', 'width': '20%'}
                    )
                ]),
                html.Div(
                    id='heatmap',
                    children = [dcc.Loading(
                        children=[HeatMap(sh, fleet_id, year, month, day, is_start, is_agg).get_scatter()],
                        type="circle")],
                    style={'height': '600px'})
                
                ]
                )