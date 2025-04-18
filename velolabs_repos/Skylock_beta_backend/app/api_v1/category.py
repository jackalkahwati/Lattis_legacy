from flask import jsonify, request
from . import api, app
from .. import db
from text import * # Text Messages
import pyexcel.ext.xls
from notification import * # Notification
from flask.ext import excel
from ..models import Category, User, MetaData, Crashdata, Firmware

admin = os.environ['ADMIN']
otp_secret = os.environ['OTP_SECRET']



@app.route('/updates/', methods=['GET'])
def get_updates():
    """ Link for update (GET) :   """
    cols = ['id', 'boot_loader']
    data = Category.query.all()
    update_key = [{col: getattr(d, col) for col in cols} for d in data]
    return jsonify(payload=update_key, status='success', message='Firmware Update')


# Backup for firmware
@app.route('/updates_1/', methods=['GET'])
def get_backupdates():
    """ Link for update (GET) :   """
    cols = ['id', 'boot_loader']
    data = Firmware.query.all()
    update_key = [{col: getattr(d, col) for col in cols} for d in data]
    return jsonify(payload=update_key, status='success', message='Firmware Update backup')
    


# ROUTE FOR LOADING LATEST FIRMWARE
@app.route('/import', methods=['GET','POST'])
def do_import():
    #Link for adding the Firmware update (POST) :
    if request.method == 'POST':
        check=MetaData.query.filter_by(id=1).first()
        hotp = pyotp.HOTP(otp_secret)

        if hotp.verify(request.form['title'], check.hint):
            try:
                num_rows_deleted = db.session.query(Category).delete()
                db.session.commit()
            except:
                db.session.rollback()   
            def category_init_func(row):
                c = Category(row['boot_loader'])
                c.id = row['id']
                return c
            request.save_book_to_database(field_name='file', session=db.session, tables=[Category], initializers=[category_init_func])
            test('title', 'New Firmware Update Available')
            db.session.query(MetaData).filter_by(id=1).update({"update_firmware":request.form['version']})
            db.session.commit()
            return jsonify(payload={}, status='success', message='Firmware Update Online')
        return jsonify(payload={}, status='error', message='Wrong Code')

    return '''
    <!doctype html>
    <title>Upload Firmware Update file</title>
    <h1>Firmware Update file upload (Note: CSV FILE ONLY)</h1>
    <form action="" method=post enctype=multipart/form-data><p>
    Firmware Version:<input type=text size=5 name=version><br>
    <p><input type=file name=file>
    Upload Verification Code:<input type=text size=10 name=title>
    <input type=submit value=Upload>
    </form>
    '''


# ROUTE for UPLOADING BACKUP FIRMWARE
@app.route('/import_1', methods=['GET','POST'])
def do_backupimport():
    # Link for adding the Firmware update (POST) :
    if request.method == 'POST':
        check=MetaData.query.filter_by(id=1).first()
        hotp = pyotp.HOTP(otp_secret)

        if hotp.verify(request.form['title'], check.hint):

            try:
                num_rows_deleted = db.session.query(Firmware).delete()
                db.session.commit()
            except:
                db.session.rollback()   
            def category_init_func(row):
                c = Firmware(row['boot_loader'])
                c.id = row['id']
                return c
            request.save_book_to_database(field_name='file', session=db.session, tables=[Firmware], initializers=[category_init_func])
            return jsonify(payload={}, status='success', message='Backup Firmware Update Online')
        return jsonify(payload={}, status='error', message='Wrong Code')

    return '''
    <!doctype html>
    <title>Upload Firmware Update file</title>
    <h1>Backup Firmware Update file upload (Note: CSV FILE ONLY)</h1>
    <form action="" method=post enctype=multipart/form-data><p>
    Firmware Version:<input type=text size=5 name=version><br>
    <p><input type=file name=file>
    Upload Verification Code:<input type=text size=10 name=title>
    <input type=submit value=Upload>
    </form>
    '''


@app.route('/export', methods=['GET'])
def export_records():
    return excel.make_response_from_tables(db.session, [User, MetaData, Crashdata, Category], "xls")
