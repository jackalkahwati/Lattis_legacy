from .main import get_dashboard_layout
import dash_core_components as dcc

import dash
from datetime import date
from .graphs import get_daily_graphs
from .heatmaps import HeatMap
from datetime import timedelta  


external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']


def get_dash_app(server, sh):
    dash_app = dash.Dash(
        __name__,
        server=server,
        external_stylesheets=external_stylesheets,
        url_base_pathname='/dash/'
    )
    #dash_app.layout = html.Div(id='dash-container')

    dash_app.layout = get_dashboard_layout(sh, fleet_id=None)

    @dash_app.callback(
    dash.dependencies.Output('daily-graphs', 'children'),
    [dash.dependencies.Input('my-date-picker-range', 'start_date'),
     dash.dependencies.Input('my-date-picker-range', 'end_date')])
    def update_daily_graphs(start_date, end_date):
        if start_date is not None and end_date is not None:
            start_date_object = date.fromisoformat(start_date)
            end_date_object = date.fromisoformat(end_date) + timedelta(days=1)
            daily_graphs = get_daily_graphs(sh=sh, fleet_id=dash_app.fleet_id, start_date=start_date_object, end_date=end_date_object)
            return daily_graphs
        else:
            return 
  


    @dash_app.callback(
    dash.dependencies.Output('heatmap', 'children'),
    [dash.dependencies.Input('heatmap_date', 'date'),
    dash.dependencies.Input('hour_selection', 'value'),
    dash.dependencies.Input('start_end_selection', 'value')])
    def update_animation(date_value, hour_selection_value, start_end_selection_value):
        #print("YO updating HEATMAP")
        if date_value is not None:
            date_object = date.fromisoformat(date_value)
            year, month, day = date_object.year, date_object.month, date_object.day
        else:
            year, month, day = 2017, 3, 26
        if hour_selection_value == 'aggregated':
            is_agg = True
        else:
            is_agg = False
        
        if start_end_selection_value == 'start':
            is_start = True
        else:
            is_start = False
        print(year, month, day, is_start, is_agg)
        return [
            dcc.Loading(
                children=[HeatMap(fleet_id=dash_app.fleet_id, sh=sh, year=year, month=month, day=day, is_start=is_start, is_agg=is_agg).get_scatter()],
                type="circle")]

    return dash_app
