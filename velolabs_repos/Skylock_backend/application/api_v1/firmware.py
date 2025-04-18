from flask import jsonify, request, abort
from . import api, application
from .. import db
from text import * # Text Messages
import pyexcel.ext.xls
from notification import * # Notification
from flask.ext import excel
from ..models import Firmware, User, MetaData, Crashdata, FirmwareBackup, Countrycode, Share, Mobile, Lock
from ..decorators import admin_required

admin = os.environ['ADMIN']
otp_secret = os.environ['OTP_SECRET']
from logging.handlers import RotatingFileHandler

#Route to GET the Firmware data from the backend
@application.route('/updates/', methods=['GET'])
def get_updates():
    """ Link for update (GET) :   """
    cols = ['id', 'boot_loader']
    data = Firmware.query.all()
    update_key = [{col: getattr(d, col) for col in cols} for d in data]
    fixes = MetaData.query.filter_by(metadata_ver='metadata').first()

    return jsonify({'payload': {'info': fixes.firmware_fixes, 'firmware': update_key}, 'status': 200, 'error':None})


# Backup for firmware
@application.route('/updates_backup/', methods=['GET'])
def get_backupdates():
    """ Link for update (GET) :   """
    cols = ['id', 'boot_loader']
    data = FirmwareBackup.query.all()
    update_key = [{col: getattr(d, col) for col in cols} for d in data]
    return jsonify({'payload':update_key, 'status': 200, 'error': None})
    


# ROUTE FOR LOADING LATEST FIRMWARE
@api.route('/import/<user_id>/', methods=['GET','POST'])
@admin_required
def do_import(user_id):
    #Link for adding the Firmware update (POST) :
    if request.method == 'POST':
        check=MetaData.query.filter_by(id=1).first()
        hotp = pyotp.HOTP(otp_secret)

        if hotp.verify(request.form['title'], check.hint):
            try:
                num_rows_deleted = db.session.query(Firmware).delete()
                db.session.commit()
            except:
                db.session.rollback()   
            def firmware_init_func(row):
                c = Firmware(row['boot_loader'])
                c.id = row['id']
                return c
            request.save_book_to_database(field_name='file', session=db.session, tables=[Firmware], initializers=[firmware_init_func])
            test('title', 'New Firmware Update Available')
            db.session.query(MetaData).filter_by(id=1).update({"update_firmware":request.form['version']})
            db.session.query(MetaData).filter_by(id=1).update({"firmware_fixes":request.form['fixes']})
            db.session.commit()
            return jsonify(payload='Firmware Update Online', status=200, error=None)
        return jsonify(payload={}, status=404, error='Wrong Code')

    return '''
    <!doctype html>
    <title>Upload Firmware Update file</title>
    <h1>Firmware Update file upload (Note: CSV FILE ONLY)</h1>
    <form action="" method=post enctype=multipart/form-data><p>
    Firmware Version: &nbsp; <input type=text size=5 name=version><br>
    <p></p>
    Fixes:<p></p>
    <textarea cols="50" rows="10" name=fixes> </textarea><br>
    <p><input type=file name=file>
    Verification Code: &nbsp; <input type=text size=10 name=title>
    <input type=submit value=Upload>
    </form>
    '''


# ROUTE for UPLOADING BACKUP FIRMWARE
@api.route('/import_backup/<user_id>/', methods=['GET','POST'])
@admin_required
def do_backupimport(user_id):
    # Link for adding the Firmware update (POST) :
    if request.method == 'POST':
        check=MetaData.query.filter_by(id=1).first()
        hotp = pyotp.HOTP(otp_secret)

        if hotp.verify(request.form['title'], check.hint):

            try:
                num_rows_deleted = db.session.query(FirmwareBackup).delete()
                db.session.commit()
            except:
                db.session.rollback()   
            def firmware_init_func(row):
                c = FirmwareBackup(row['boot_loader'])
                c.id = row['id']
                return c
            request.save_book_to_database(field_name='file', session=db.session, tables=[FirmwareBackup], initializers=[firmware_init_func])
            return jsonify(error=None, status=200, payload='Backup Firmware Update Online')
        return jsonify(payload={}, status=404, error='Wrong Code')

    return '''
    <!doctype html>
    <title>Upload Firmware Update file</title>
    <h1>Backup Firmware Update file upload (Note: CSV FILE ONLY)</h1>
    <form action="" method=post enctype=multipart/form-data><p>
    Firmware Version:<input type=text size=5 name=version><br>
    <p><input type=file name=file>
    Verification Code:<input type=text size=10 name=title>
    <input type=submit value=Upload>
    </form>
    '''



@api.route('/country_code/<user_id>/', methods=['GET','POST'])
@admin_required
def import_countrycode(user_id):
    if(g.user == user_id):
        #Link for adding the Firmware update (POST) :
        if request.method == 'POST':
            check=MetaData.query.filter_by(id=1).first()
            hotp = pyotp.HOTP(otp_secret)

            if hotp.verify(request.form['title'], check.hint):
                try:
                    num_rows_deleted = db.session.query(Countrycode).delete()
                    db.session.commit()
                except:
                    db.session.rollback()   
                def countrycode_init_func(row):
                    c = Countrycode(row['telephone_code'], row['letter_code'], row['common_name'])

                    return c
                request.save_book_to_database(field_name='file', session=db.session, tables=[Countrycode], initializers=[countrycode_init_func])
                test('title', 'Country codes Updated')
                #db.session.query(MetaData).filter_by(id=1).update({"update_firmware":request.form['version']})
                db.session.commit()
                return jsonify(payload='Countrycode Update Online', status=200, error=None)
            return jsonify(payload={}, status=404, error='Wrong Code')

        return '''
        <!doctype html>
        <title>Upload Country Code file</title>
        <h1>Country Code file upload (Note: CSV FILE ONLY)</h1>
        <form action="" method=post enctype=multipart/form-data><p>
        <p><input type=file name=file>
        Verification Code:<input type=text size=10 name=title>
        <input type=submit value=Upload>
        </form>
        '''
    else:
        return abort(403)


# Route to get the Firmware Version on the backend
@api.route('/users/<user_id>/firmwareversion/', methods=['GET'])
def get_version(user_id):
    if (g.user == user_id):
        version = MetaData.query.filter_by(metadata_ver="metadata").first()
        if version:
            return jsonify({'status': 201, 'error': None, 'payload': {'firmware_version': version.export_firmware_version()  }})
        else:
            return abort(404)
    else:
        return abort(403)




@api.route('/export/<user_id>/', methods=['GET'])
@admin_required
def export_records(user_id):
    if(g.user == user_id):
        return excel.make_response_from_tables(db.session, [MetaData, Crashdata, Firmware, Share, Mobile], "xls")
    else:
        return abort(403)


@api.route("/export_user/<user_id>/", methods=['GET'])
@admin_required
def douserexport(user_id):
    if(g.user == user_id):
        query_sets = User.query.all()
        column_names = ['first_name', 'last_name', 'date_created', 'email', 'verified', 'user_id', 'maxLocks', 'country_code' ]
        return excel.make_response_from_query_sets(query_sets, column_names, "xls")
    else:
        return abort(403)


@api.route("/export_locks/<user_id>/", methods=['GET'])
@admin_required
def doclockexport(user_id):
    if(g.user == user_id):
        query_sets = Lock.query.all()
        column_names = ['key_id', 'users_id', 'user_id', 'mac_id', 'uuid', 'lock_name' ]
        return excel.make_response_from_query_sets(query_sets, column_names, "xls")
    else:
        return abort(403)