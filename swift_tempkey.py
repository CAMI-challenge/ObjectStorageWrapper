#!/usr/bin/python

import hmac
from hashlib import sha1
# from time import time
import time
method = 'GET'
# duration_in_seconds = 60*60*24
duration_in_seconds = 180
expires = int(time.time() + duration_in_seconds)
path = '/swift/v1/marmg_cami2/reads'
key = CHANGE_ME (quote-delimited string - long)
hmac_body = '%s\n%s\n%s' % (method, expires, path)
signature = hmac.new(key, hmac_body, sha1).hexdigest()
utc_time=time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime(expires))
print "https://openstack.cebitec.uni-bielefeld.de:8080"+path + "?temp_url_sig="+signature+"&temp_url_expires="+str(expires)
