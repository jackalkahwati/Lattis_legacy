import traceback
import dash_core_components as dcc
import dash_html_components as html
import pandas as pd 
import plotly.express as px
from datetime import date, timedelta
import datetime
import dash_bootstrap_components as dbc

def make_grid(graphs):
    rows = []
    for i in range(0, len(graphs), 2):
        rows.append(
            dbc.Row(
                [g for g in graphs[i:min(i+2, len(graphs))]]
                ))
    return html.Div(rows)


def get_graphs(sh, fleet_id):
    start_date=date.today() - timedelta(days=30)
    end_date=date.today()

    daily_graphs = get_daily_graphs(sh, fleet_id, start_date, end_date)
    return dcc.Loading(
            id="loading",
            children=[html.Div(children=daily_graphs, id='daily-graphs')],
            type="circle")

    
def graph_from_fig(fig):
    return dcc.Graph(figure=fig, style = {'display': 'inline-block', 'width': '48%'})   


def get_daily_graphs(sh, fleet_id, start_date, end_date):
    figs = []
    t_start = datetime.datetime.combine(start_date, datetime.datetime.min.time())
    t_end = datetime.datetime.combine(end_date, datetime.datetime.min.time())

    df = sh.get_daily_data(fleet_id, t_start, t_end)
    figs.append(px.line(df, x="date", y=['active', 'n_with_a_trip'], title='Vehicles'))
    figs.append(px.line(df, x="date", y=['utilization', 'activation'], title='Activation / Utilization'))
    figs.append(px.line(df, x="date", y=['n_trips', 'n_valid', 'n_valid_under_2_hours'], title='Trips'))
    try:
        figs.append(px.line(df, x="date", y=['avg_duration', 'avg_duration_valid', 'avg_duration_valid_under_2'], title='Duration'))
    except:
        traceback.print_exc()
    try: 
        figs.append(px.line(df, x="date", y=['avg_valid_trip_ticket', 'gross_revenue', 'gross_revenue_under_2_hours'], title='Revenues'))
    except:
        traceback.print_exc()
    try:  
        figs.append(px.line(df, x="date", y=['n_unique_users', 'avg_trips_per_users'], title='Users'))
    except:
        traceback.print_exc()
    

    graphs = [graph_from_fig(f) for f in figs]
    return make_grid(graphs)
