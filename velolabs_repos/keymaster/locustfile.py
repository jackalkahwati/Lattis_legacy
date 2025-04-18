# from locust import HttpLocust, TaskSet, task
#
# #class HttpSession(base_url, *args, **kwargs)
#
#
# class MyTaskSet(TaskSet):
#     @task
#     def my_task(self):
#         self.client.get("/users/", auth=HTTPBasicAuth('eyJhbGciOiJIUzI1NiIsImV4cCI6MTQ3NjM5NDMxNSwiaWF0IjoxNDY4NjE4MzE1fQ.eyJ1c2VybmFtZSI6IjQxNTY3Njc5MjEifQ.fX_K7Gwnl87S1VdsWH6-tLYJ4WBiwjxmclEO75vthcA', ''))
#
#
# class MyLocust(HttpLocust):
#     task_set = MyTaskSet
#     min_wait=5000
#     max_wait=9000
#     host = "http://localhost:5000/api/v1/"