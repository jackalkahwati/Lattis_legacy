# lattis-datascience
Repo for pricing and repositioning microservice



## Installing your python environment

On Ubuntu :
 - Install pip

 `sudo apt install python-pip`

 - Install the requirements for this repo :

 `pip install -r requirements.txt`  (you might need root access : sudo )

 - If you encounter errors while installing the requirements, make sure that you have the following packages installed on your system :

 `sudo apt install libpq-dev`



## Installing your python environment

On Ubuntu :
 - Install pip

 `sudo apt install python-pip`

 - Install the requirements for this repo :

 `pip install -r requirements.txt`  (you might need root access : sudo )

 - If you encounter errors while installing the requirements, make sure that you have the following packages installed on your system :

 `sudo apt install libpq-dev`


## Running docker for the Postgres DB

 Run the Postgres docker alone.

 `docker run --rm --name local_pg_db -e POSTGRES_PASSWORD=docker -d -p 5433:5432 postgres:11-alpine`

 install psql
 `sudo apt-get install postgresql-client`

 You can now us psql to connect (pass docker)

 `psql -h localhost -U postgres -d postgres -p 5433`


# Launching The API

uvicorn main:app --reload
