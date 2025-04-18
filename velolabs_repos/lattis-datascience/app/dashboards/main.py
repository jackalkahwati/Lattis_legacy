
import pandas as pd 
import plotly.express as px
import dash_core_components as dcc
import dash_html_components as html

from .date_picker import get_date_picker
from .heatmaps import get_heat_map
from .graphs import get_graphs

def get_dashboard_layout(sh, fleet_id):

    if fleet_id is None: 
        return html.Div(children=[html.H1(children='Lattis Fleet Analytics')])

    

    return html.Div(children=[
        html.H1(children='Lattis Fleet Analytics'),
        html.Div(children=f"Fleet Name :  {sh.get_fleet_id_names()[fleet_id]}"),
        get_date_picker(),
        get_graphs(sh, fleet_id=fleet_id),
        get_heat_map(sh, fleet_id=fleet_id)
    ])