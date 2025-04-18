import os
from app import create_app, db
from app.models import User, Role, MetaData


app = create_app(os.environ.get('FLASK_CONFIG', 'development'))

with app.app_context():
	db.drop_all()
	db.create_all()
	Role.insert_roles()
	if MetaData.query.get(1) is None:
        	u = MetaData(metadata_ver='metadata', hint=0)
        	db.session.add(u)
        	db.session.commit()