FROM tiangolo/uvicorn-gunicorn-fastapi:python3.7

COPY ./app /app

COPY ./lattis_ds /app/lattis_ds/

RUN pip3 install -r requirements.txt

COPY ./setup.py /app/setup.py

RUN ls

RUN  python setup.py install
