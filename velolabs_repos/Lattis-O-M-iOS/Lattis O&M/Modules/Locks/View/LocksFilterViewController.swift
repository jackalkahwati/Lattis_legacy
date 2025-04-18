//
//  LocksFilterViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 8/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class LocksFilterViewController: ViewController {
    weak var delegate: LocksFilterSectionDelegate?
    fileprivate var selected: Lock.Filter = .all
    fileprivate let filters: [Lock.Filter] = [.all, .bike, .noBike]
    fileprivate let vendors: [Lock.Vendor] = [.ellipse, .axa]
    fileprivate var selectedVendor: Lock.Vendor = .ellipse
    fileprivate let doneButton = UIButton(type: .custom)
    fileprivate let tableView = UITableView()
    
    init(delegate: LocksFilterSectionDelegate, filter: Lock.Filter = .all, vendor: Lock.Vendor = .ellipse) {
        self.delegate = delegate
        selected = filter
        selectedVendor = vendor
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "locks_filter_title".localized()

        tableView.register(LocksFilterCell.self, forCellReuseIdentifier: "cell")
        view.backgroundColor = .white
        view.addSubview(tableView)
        view.addSubview(doneButton)
        constrain(tableView, doneButton, view) { (table, done, view) in
            table.top == view.safeAreaLayoutGuide.top
            table.left == view.left
            table.right == view.right
            
            done.bottom == view.safeAreaLayoutGuide.bottom - .margin/2
            done.left == view.left + .margin/2
            done.right == view.right - .margin/2
            done.height == 44
            
            table.bottom == done.top - .margin/2
        }
        
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        tableView.contentInset = UIEdgeInsets(top: 30, left: 0, bottom: 0, right: 0)
        
        doneButton.setTitle("done".localized(), for: .normal)
        doneButton.layer.cornerRadius = 5
        doneButton.backgroundColor = .lsTurquoiseBlue
        doneButton.addTarget(self, action: #selector(done), for: .touchUpInside)
        
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
    }
    
    @objc
    fileprivate func close() {
        dismiss(animated: true, completion: nil)
    }
    
    @objc
    fileprivate func done() {
        delegate?.didSelect(filter: selected, vendor: selectedVendor)
        close()
    }
}

extension LocksFilterViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 0 {
            return filters.count
        }
        return vendors.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! LocksFilterCell
        if indexPath.section == 0 {
            let filter = filters[indexPath.row]
            cell.textLabel?.text = filter.title
            cell.imageView?.image = filter == selected ? #imageLiteral(resourceName: "icon_radion_selected") : #imageLiteral(resourceName: "icon_radio_unselected")
        } else {
            let vendor = vendors[indexPath.row]
            cell.textLabel?.text = vendor.title
            cell.imageView?.image = vendor == selectedVendor ? #imageLiteral(resourceName: "icon_radion_selected") : #imageLiteral(resourceName: "icon_radio_unselected")
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.section == 0 {
            selected = filters[indexPath.row]
        } else {
            selectedVendor = vendors[indexPath.row]
        }
        tableView.reloadData()
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if section == 0 {
            return nil
        }
        return SectionView("section_vendor".localized())
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 0 {
            return 0
        }
        return 44
    }
}

fileprivate class SectionView: UIView{
    fileprivate let titleLabel = UILabel()
    
    init(_ title: String) {
        titleLabel.text = title
        super.init(frame: .zero)
        
        backgroundColor = .white
        addSubview(titleLabel)
        
        constrain(titleLabel, self) { label, view in
            label.edges == view.edges.inseted(by: .margin)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class LocksFilterCell: UITableViewCell {
    
}
