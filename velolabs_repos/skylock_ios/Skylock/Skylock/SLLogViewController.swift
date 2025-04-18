//
//  SLLogViewController.swift
//  Skylock
//
//  Created by Andre Green on 5/14/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLLogViewController:UIViewController, UITableViewDelegate, UITableViewDataSource {
    var logs:[SLLog] = [SLLog]()
    let dateFormatter:DateFormatter = DateFormatter()
    
    lazy var tableView:UITableView = {
        let table:UITableView = UITableView(frame: self.view.bounds, style: UITableViewStyle.plain)
        table.rowHeight = 80.0
        table.delegate = self
        table.dataSource = self
        
        return table
    }()
    
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.addSubview(self.tableView)
        let backButton:UIBarButtonItem = UIBarButtonItem(
            barButtonSystemItem: UIBarButtonSystemItem.done,
            target: self,
            action: #selector(backButtonPressed)
        )
        
        self.navigationItem.leftBarButtonItem = backButton;
        self.dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        let databaseManager = SLDatabaseManager.shared()
        self.logs = databaseManager.getAllLogs() as! [SLLog]
        self.logs.sort(by: {$0.date!.compare($1.date!) == .orderedDescending})
        
        NotificationCenter.default.addObserver(
            forName: Notification.Name(rawValue: kSLNotificationLogUpdated),
            object: nil,
            queue: nil,
            using: newLogAdded
        )
    }
    
    func backButtonPressed() {
        self.navigationController!.popViewController(animated: true)
    }
    
    func newLogAdded(notification: Notification) {
        if let info = notification.object as? [String:SLLog], let log = info["log"] {
            self.logs.insert(log, at: 0)
            let newIndexPath = IndexPath(row: 0, section: 0)
            self.tableView.insertRows(at: [newIndexPath], with: UITableViewRowAnimation.automatic)
        }
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.logs.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellId = "SLLogViewControllerCell"
        var cell: UITableViewCell? = tableView.dequeueReusableCell(withIdentifier: cellId)
        if cell == nil {
            cell = UITableViewCell(style: UITableViewCellStyle.subtitle, reuseIdentifier: cellId)
        }
        
        let log = self.logs[indexPath.row]
        cell!.textLabel?.text = log.entry
        cell!.detailTextLabel?.text = self.dateFormatter.string(from: log.date!)
        
        return cell!
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let log = self.logs[indexPath.row]
        let dvc:SLLogDetailViewController = SLLogDetailViewController(nibName: nil, bundle: nil, log: log)
        self.navigationController?.pushViewController(dvc, animated: true)
    }
}
