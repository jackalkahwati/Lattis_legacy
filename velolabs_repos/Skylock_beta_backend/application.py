#!/usr/bin/env python
import os
from app import create_app, db
from app.models import User, MetaData


if __name__ == '__main__':
    application = app = create_app(os.environ.get('FLASK_CONFIG', 'development'))
    
    with app.app_context():
        db.create_all()
        if MetaData.query.get(1) is None:
        	u = MetaData(metadata_ver='metadata')
        	db.session.add(u)
        	db.session.commit()
    application.run()
