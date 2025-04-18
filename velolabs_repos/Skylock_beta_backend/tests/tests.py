import unittest
from werkzeug.exceptions import NotFound
from app import create_app, db
from app.models import *
from .test_client import TestClient



class TestAPI(unittest.TestCase):
    default_username = '4156767921'
    default_password = 'shiva'

    def setUp(self):
        self.app = create_app('testing')
        self.ctx = self.app.app_context()
        self.ctx.push()
        db.drop_all()
        db.create_all()
        u = User(username=self.default_username)
        u.set_password(self.default_password)
        db.session.add(u)
        db.session.commit()
        self.client = TestClient(self.app, u.generate_auth_token(), '')

    def tearDown(self):
        db.session.remove()
        db.drop_all()
        self.ctx.pop()

    def test_users(self):

        # add a user
        rv, json = self.client.post('/api/v1/users/',
                                    data={'first_name': 'shiva', 'last_name': 'aratal', 'user_id': '4156767921', 'password': 'shiva', 'fb_flag': '0', 'reg_id': 'abc'})
        self.assertTrue(rv.status_code == 201)
        location = rv.headers['Location']
        rv, json = self.client.get(location)
        self.assertTrue(rv.status_code == 200)
        self.assertTrue(json['first_name'] == 'shiva')
        self.assertTrue(json['last_name'] == 'aratal')
        self.assertTrue(json['user_id'] == '4156767921')
        self.assertTrue(json['password'] == 'shiva')
        self.assertTrue(json['fb_flag'] == '0')
        self.assertTrue(json['reg_id'] == 'abc')

        rv, json = self.client.get('/api/v1/users/')
        self.assertTrue(rv.status_code == 200)
        self.assertTrue(json['payload'] == [location])

        # edit the customer
        rv, json = self.client.put(location, data={'name': 'John Smith'})
        self.assertTrue(rv.status_code == 200)
        rv, json = self.client.get(location)
        self.assertTrue(rv.status_code == 200)
        self.assertTrue(json['name'] == 'John Smith')

    