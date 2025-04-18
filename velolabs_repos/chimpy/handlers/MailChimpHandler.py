from handlers.RestHandler import RestHandler
from handlers.DatabaseHandler import DatabaseHandler
from models.EmailCode import EmailCode
from config import ChimpyConfig
import os
from tools.Hasher import md5_hasher
import json


class MailChimpHandler:
    def __init__(self):
        self.config = ChimpyConfig()
        self.rest_handler = RestHandler()
        self.db_handler = DatabaseHandler()
        self.list_update_index = 0
        self.MAX_MEMBER_UPDATE = 500
        self.email_codes = []
        self.batch_ids = []

    def _headers(self):
        return {
            'Authorization': self.rest_handler.basicAuthHeader('', self.config.CHIMPY_MAILCHIMP_API_KEY)
        }

    def get_all_lists(self):
        params = self.rest_handler.construct_query_string({'count': 20})
        lists = self.rest_handler.get_request(
            url=self.config.mail_chimp_api_base_url() + '/lists/' + params,
            headers=self._headers()
        )
        return lists

    def get_list(self, list_id):
        list_info = self.rest_handler.get_request(
            url=self.config.mail_chimp_api_base_url() + '/lists/' + list_id,
            headers=self._headers()
        )
        print(list_info)
        return None

    def get_members(self, list_id):
        url = self.config.mail_chimp_api_base_url() + 'lists/' + list_id + '/members'
        print('URL:', url)
        members_response = self.rest_handler.get_request(
            url=url,
            headers=self._headers()
        )
        return members_response['members']

    def update_list(self):
        # path = os.path.join(os.path.dirname(__file__), '../files/members.json')
        # with open(path, 'r') as member_file:
        #     members = json.load(member_file)
        # member_file.close()

        # Reset the error file
        path = os.path.join(os.path.dirname(__file__), '../files/batch_errors.json')
        with open(path, 'w') as batch_file:
            json.dump({}, batch_file)
        batch_file.close()
        self.batch_ids = []
        members = self.create_email_list_members()
        self._update_list_members_helper(self.config.mailchimp_list_ids.EMAIL_UPDATE, members)
        return None

    def _update_list_members_helper(self, list_id, members):
        if len(members) > self.MAX_MEMBER_UPDATE:
            end_index = self.MAX_MEMBER_UPDATE
        else:
            end_index = len(members)
        print('Bulk updating', end_index, 'members')
        batch_members = []
        for member in members[:end_index]:
            batch_members.append({
                'method': 'PUT',
                'path': 'lists/%s/members/%s' % (list_id, md5_hasher(member['email_address'].lower())),
                'body': json.dumps(member)
            })
        response = self.rest_handler.json_post_request(
            url=os.path.join(self.config.mail_chimp_api_base_url(), 'batches'),
            post_object={'operations': batch_members},
            headers=self._headers()
        )
        self.batch_ids.append(response['id'])
        members = members[end_index:]
        print(response)
        print(len(members), 'remaining to update\n')
        if len(members) == 0:
            path = os.path.join(os.path.dirname(__file__), '../files/batch_ids.json')
            with open(path, 'w') as batch_file:
                json.dump(self.batch_ids, batch_file)
            batch_file.close()
        else:
            self._update_list_members_helper(list_id, members)
        return None

    def get_email_codes(self):
        email_codes = self.db_handler.select(table='email_code')
        self.email_codes = [EmailCode(email_code) for email_code in email_codes]
        # index = 0
        # for code in self.email_codes:
        #     if code.email == 'ronanstafford@gmail.com':
        #         break
        #     index += 1
        # self.email_codes = self.email_codes[index:]
        return None

    def get_batch_status(self, batch_id):
        return self.rest_handler.get_request(
            url=os.path.join(self.config.mail_chimp_api_base_url(), 'batches', batch_id),
            headers=self._headers()
        )

    def get_batch(self, url):
        return self.rest_handler.get_request(
            url=url,
            headers=self._headers()
        )

    def get_batch_errors(self):
        path = os.path.join(os.path.dirname(__file__), '../files/batch_ids.json')
        with open(path, 'r') as batch_id_file:
            batch_ids = json.load(batch_id_file)
        batch_id_file.close()
        [self.save_batch_status(self.get_batch_status(batch_id)) for batch_id in batch_ids]
        return None

    def save_batch_status(self, batch_data):
        path = os.path.join(os.path.dirname(__file__), '../files/batch_errors.json')
        with open(path, 'r+') as batch_file:
            batch_dict = json.load(batch_file)
            batch_dict[batch_data['id']] = {
                'errored_operations': batch_data['errored_operations'],
                'status': batch_data['status'],
                'finished_operations': batch_data['finished_operations'],
                'total_operations': batch_data['total_operations'],
                'body_url': batch_data['response_body_url']
            }
            batch_file.seek(0)
            batch_file.truncate()
            json.dump(batch_dict, batch_file)
        batch_file.close()
        return None

    def get_batch_error_info(self):
        path = os.path.join(os.path.dirname(__file__), '../files/batch_errors.json')
        total_completed = 0
        total_errors = 0
        with open(path, 'r') as batch_file:
            batch_dict = json.load(batch_file)
        batch_file.close()
        for batch_id, batch_info in batch_dict.items():
            if batch_info['status'] == 'finished':
                total_completed += batch_info['total_operations']
                total_errors += batch_info['errored_operations']
        return {
            'completed': total_completed,
            'errors': total_errors
        }

    def create_email_list_members(self):
        members = []
        self.get_email_codes()
        for email_code in self.email_codes:
            if not email_code.email or email_code.email == '':
                continue
            members.append({
                'email_address': email_code.email,
                'status': 'subscribed',
                'merge_fields': {
                    'FNAME': email_code.first_name if email_code.first_name else '',
                    'LNAME': email_code.last_name if email_code.last_name else '',
                    'UPDATEURL': self._test_code_url(email_code.email_code),
                    'EMAILCODE': str(email_code.code)
                }
            })
        return members

    def _test_code_url(self, code):
        return self.config.EMAIL_UPDATE_URL + self.rest_handler.construct_query_string({'code': code})
