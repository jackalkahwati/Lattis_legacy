import dash_html_components as html
import dash_core_components as dcc
from datetime import date, timedelta
import datetime

def get_date_picker(default_date=()):
    return html.Div(children=[
        'Date range : ',    
        dcc.DatePickerRange(
            id='my-date-picker-range',
            min_date_allowed=date(1995, 8, 5),
            max_date_allowed=datetime.datetime.now().date(),
            end_date=date.today(),
            display_format='MMM Do, YY',
            start_date=date.today() - timedelta(days=30))
        ])  
