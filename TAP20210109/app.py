from flask import Flask, request
from flask import jsonify
from flask_sqlalchemy import SQLAlchemy
import pymysql
from collections import OrderedDict
pymysql.install_as_MySQLdb()
import json
import sys

DEBUG_MODE = False
if len(sys.argv) > 1 and sys.argv[1] == "debug":
    DEBUG_MODE = True

app = None
if not DEBUG_MODE:   
    from gevent import monkey
    monkey.patch_all()
    from gevent.pywsgi import WSGIServer
    from multiprocessing import cpu_count, Process

app = Flask(__name__)

mysql_user = "root"
mysql_passwd = "123456"
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://' + mysql_user + ':' + mysql_passwd + '@localhost/medical'
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN'] = True
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = True
db = SQLAlchemy(app)

class Data_article(db.Model):
    __tablename__ = 'article'
    article_id = db.Column(db.String, primary_key=True)
    title = db.Column(db.String)
    author = db.Column(db.String)
    publication = db.Column(db.String)
    abstract = db.Column(db.String)

def data_article_to_dict(Data):
    return OrderedDict(
        article_id=Data.article_id,
        title=Data.title,
        author=Data.author,
        publication=Data.publication,
        abstract=Data.abstract,
    )

@app.route('/article', methods=['GET'])
def get_data_user():
    MyData = Data_article.query.all()
    js = list(map(data_article_to_dict, MyData))
    with open('article.json', 'w') as f:
        f.write(json.dumps(js))
    return(jsonify(js))

class Data_label(db.Model):
    __tablename__ = 'label'
    #label_id=db.Column(db.String,primary_key=True,autoincrement=True)
    article_id = db.Column(db.String, db.ForeignKey("article.article_id"), primary_key = True)
    start_index = db.Column(db.Integer, primary_key = True)
    end_index = db.Column(db.Integer, primary_key = True)
    label = db.Column(db.String, primary_key = True)
    text = db.Column(db.String)

    #author = db.relationship('article', backref=db.backref('articles'))

def data_label_to_dict(Data):
    return OrderedDict(
        #label_id=Data.label_id,
        article_id=Data.article_id,
        start_index=Data.start_index,
        end_index=Data.end_index,
        label=Data.label,
        text=Data.text
    )

@app.route('/label', methods=['GET'])
def get_data_label():
    MyData = Data_label.query.all()
    js = list(map(data_label_to_dict, MyData))
    with open('label.json', 'w') as f:
        f.write(json.dumps(js))
    return(jsonify(js))

db.create_all()

@app.route('/article/search', methods=['POST'])
def search_article():
    if request.method == 'POST':
        title = request.form.get('title')
        author = request.form.get('author')
        temp=Data_article.query.filter_by(title=title, author=author)
        js = list(map(data_article_to_dict, temp))
        return (jsonify(js))

@app.route('/article/test_exists') 
def test_exists():
    aid = request.args.get("article_id")
    articles = Data_article.query.filter_by(article_id=aid).first()
    if articles != None:
        return "1"
    else:
        return "0"

@app.route('/article/add', methods=['POST'])
def add_article():
    if request.method == 'POST':
        article_id=request.form.get('article_id')
        title = request.form.get('title')
        author = request.form.get('author')
        publication=request.form.get('publication')
        #abstract = db.Column(db.text)
        abstract=request.form.get('abstract')

        #print(article_id)
        #print(title)
        #print(author)
        #print(publication)
        #print(abstract)

        article1=Data_article(article_id=article_id,title=title,author=author,publication=publication,abstract=abstract)
        
        try:
            db.session.add(article1)
            db.session.commit()
        except:
            return "0"
        return "1"

@app.route('/article/update', methods=['POST'])
def update_article():
    if request.method == 'POST':
        article_id=request.form.get('article_id')
        title = request.form.get('title')
        author = request.form.get('author')
        publication=request.form.get('publication')
        abstract = db.Column(db.text)

        article1 = Data_article.query.filter(Data_article.title == title).first()
        article1.article_id = article_id
        article1.title = title
        article1.author = author
        article1.publication = publication
        article1.abstract = abstract
        db.session.commit()

@app.route('/article/delete', methods=['POST'])
def delete_test():
    if request.method == 'POST':
        title = request.form.get('title')
        article1 = Data_article.query.filter(Data_article.title == title).first()
        db.session.delete(article1)
        db.session.commit()

@app.route('/article/upload_labels', methods=['POST'])
def upload_article_labels():
    data = json.loads(request.form.get('data'))
    print(data)
    try:
        for label in data["data"]:
            lb = Data_label(article_id=label["article_id"], 
                            start_index=label["start_index"],
                            end_index=label["end_index"],
                            label=label["label"],
                            text=label["text"])
            db.session.add(lb)
        db.session.commit()
    except Exception as e:
        print(e)
        return "0"
    return "1"

# 解决跨域问题用的
@app.after_request
def cors(environ):
    environ.headers['Access-Control-Allow-Origin']='*'
    environ.headers['Access-Control-Allow-Method']='*'
    environ.headers['Access-Control-Allow-Headers']='x-requested-with,content-type'
    return environ

if __name__ == '__main__':
    if not DEBUG_MODE:
        server = WSGIServer(('', 5000), app).serve_forever()
    else:
        app.run(host="0.0.0.0", port=5000, threaded=True, debug=True)
