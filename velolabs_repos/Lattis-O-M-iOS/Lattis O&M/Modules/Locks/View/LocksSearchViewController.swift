//
//  LocksSearchViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26/03/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import UIKit
import Cartography

final class LocksSearchCell: UITableViewCell {
    fileprivate let titleLabel = UILabel()
    fileprivate let subtitleLabel = UILabel()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        selectionStyle = .none
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(subtitleLabel)
        
        titleLabel.font = .systemFont(ofSize: 14)
        subtitleLabel.font = .systemFont(ofSize: 12)
        titleLabel.textColor = .lsSteel
        subtitleLabel.textColor = .lsWarmGrey
        
        constrain(titleLabel, subtitleLabel, contentView) { title, subtitle, view in
            title.top == view.top + 15
            title.left == view.left + 15
            title.right == view.right + 15
            
            subtitle.bottom == view.bottom - 15
            subtitle.left == title.left
            subtitle.right == title.right
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var lock: Lock! {
        didSet {
            titleLabel.text = lock.lock == nil ? "Not Onboarded" : lock.displayTitle
            subtitleLabel.text = lock.macId
        }
    }
}

protocol LocksSearchDelegate: class {
    func locksSearch(controller: LocksSearchViewController, didSelect lock: Lock)
}

final class LocksSearchViewController: ViewController {
    weak var delegate: LocksSearchDelegate?
    
    init(locks: [Lock], delegate: LocksSearchDelegate) {
        self.delegate = delegate
        self.initialLocks = locks
        self.locks = locks
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate let initialLocks: [Lock]
    fileprivate let tableView = UITableView()
    fileprivate var locks: [Lock] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        view.addSubview(tableView)
        tableView.register(LocksSearchCell.self, forCellReuseIdentifier: "cell")
        tableView.dataSource = self
        tableView.delegate = self
        tableView.tableFooterView = UIView()
        tableView.rowHeight = 64
        
        let bar = UISearchBar()
        bar.placeholder = "Mac Id or Lock Name"
        navigationItem.titleView = bar
        bar.delegate = self
        
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
        bar.becomeFirstResponder()
    }
    
    @objc func close() {
        dismiss(animated: true, completion: nil)
    }
}

extension LocksSearchViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return locks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! LocksSearchCell
        cell.lock = locks[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let lock = locks[indexPath.row]
        let alert = UIAlertController(title: "Connect?", message: lock.macId, preferredStyle: .alert)
        alert.addAction(.init(title: "Yes", style: .default, handler: { _ in
            self.delegate?.locksSearch(controller: self, didSelect: lock)
        }))
        alert.addAction(.init(title: "Cancel", style: .destructive, handler: nil))
        present(alert, animated: true, completion: nil)
    }
}

extension LocksSearchViewController: UISearchBarDelegate {
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        let pattern = searchText
        if !pattern.isEmpty {
            locks = initialLocks.filter({ (lock) -> Bool in
                if let name = lock.lock?.name, name.lowercased().contains(pattern.lowercased()) { return true }
                if let macId = lock.peripheral?.macId, macId.lowercased().contains(pattern.lowercased()) { return true }
                return false
            })
        } else {
            locks = initialLocks
        }
        tableView.reloadData()
    }
}
