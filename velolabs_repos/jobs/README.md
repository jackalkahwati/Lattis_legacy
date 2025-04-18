# jobs
Repo to help manage cron jobs for other apps

- Cron jobs cause problems when apps run on multiple instances in AWS on on multiple cores using PM2. To fix this issue, this repo will purely be a utility repo to run on a single instance always and do light jobs. The main apps will still be responsible for handling the cron logic.

- For instance, the GPS service needs to ping the dashboard every 5 minutes to update GPS locations for out of ride vehicles. We run this using a cron job. Since the GPS service runs on a 12 core CPU, 12 duplicate requests are send and executed by the dashboard backend. If we have 3 instances of this with each running a 12 core CPU, 36 request will be made. If it's to update 100 vehicles, the updates go from 100 to 3*12*100=3600. We made 3500 extra DB writes with the same information.

- To solve the above issue, the jobs service will run the cron job and dispatch the request. This service should always run on a single instance and handle light jobs.

- If we need to update the dashboard with GPS data, the jobs service runs a cron job and triggers a HTTP request to the GPS service. This way the AWS load balancer will handle where the request will go. In case PM2 is involved, it's load balancer knows how to not duplicate request handling. A request will contain the name of the operation to be executed. For example, if the operation is update vehicle locations in the dashboard, we can send a request with parameters `{"operation": "update_dashboard_GPS_locations"}` and based on that, the GPS service can run the function that does this.


