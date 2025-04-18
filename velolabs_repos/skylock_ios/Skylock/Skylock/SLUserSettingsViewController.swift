//
//  SLUserSettingsViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/22/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

class SLUserSettingsViewController:
UIViewController,
UITableViewDelegate,
UITableViewDataSource,
SLUserSettingTableViewCellDelegate
{
    private enum TextLocation {
        case Top
        case Bottom
    }
    
    private let settings:[[TextLocation:String]] = [
        [
            .Top: NSLocalizedString("Theft detection", comment: ""),
            .Bottom: NSLocalizedString("When a theft has been detected", comment: "")
        ],
        [
            .Top: NSLocalizedString("Crash detection", comment: ""),
            .Bottom: NSLocalizedString("When a crash has detected", comment: "")
        ],
        [
            .Top: NSLocalizedString("Auto proximity unlock", comment: ""),
            .Bottom: NSLocalizedString("When my ellipse is automatically unlocked", comment: "")
        ],
        [
            .Top: NSLocalizedString("Low battery", comment: ""),
            .Bottom: NSLocalizedString("When my ellipse battery reaches 20%", comment: "")
        ],
        [
            .Top: NSLocalizedString("Low battery", comment: ""),
            .Bottom: NSLocalizedString("When my ellipse battery reaches 20%", comment: "")
        ]
    ]
    
    lazy var tableView:UITableView = {
        let table:UITableView = UITableView(frame: self.view.bounds, style: .plain)
        table.delegate = self
        table.dataSource = self
        table.rowHeight = 80.0
        table.backgroundColor = UIColor.white
        table.register(
            SLUserSettingTableViewCell.self,
            forCellReuseIdentifier: String(describing: SLUserSettingTableViewCell.self)
        )
        
        return table
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationItem.title = NSLocalizedString("MY PROFILE", comment: "")
        self.view.addSubview(self.tableView)
    }
    
    // MARK: UITableView Delegate and Datasource methods
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.settings.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellId = String(describing: SLUserSettingTableViewCell.self)
        let cell:SLUserSettingTableViewCell? = tableView.dequeueReusableCell(withIdentifier: cellId)
            as? SLUserSettingTableViewCell
        cell?.delegate = self
        
        if let topText = self.settings[indexPath.row][.Top] {
            cell?.textLabel?.text = topText
        }
        
        if let bottomText = self.settings[indexPath.row][.Bottom] {
            cell?.detailTextLabel?.text = bottomText
        }
        
        return cell!
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 80.0
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let viewFrame = CGRect(
            x: 0,
            y: 0,
            width: tableView.bounds.size.width,
            height: self.tableView(tableView, heightForHeaderInSection: 0)
        )
        
        let view:UIView = UIView(frame: viewFrame)
        
        let xPadding:CGFloat = 10.0
        let height:CGFloat = 20.0
        let textColor = UIColor(red: 140, green: 140, blue: 140)
        let labelFrame = CGRect(
            x: xPadding,
            y: 0.5*(view.bounds.size.height - height),
            width: viewFrame.size.width - 2*xPadding,
            height: height
        )
        
        let label:UILabel = UILabel(frame: labelFrame)
        label.font = UIFont.systemFont(ofSize: 18.0)
        label.text = NSLocalizedString("Push notifications", comment: "")
        label.textColor = textColor
        
        view.addSubview(label)
        
        let dividerViewFrame = CGRect(
            x: 0.0,
            y: view.bounds.size.height - 1,
            width: view.bounds.size.width,
            height: 1
        )
        
        let dividerView = UIView(frame: dividerViewFrame)
        dividerView.backgroundColor = textColor
        
        view.addSubview(dividerView)
        
        return view
    }
    
    // Mark: SLUserSettingsTableViewCellDelegate methods
    func userSettingsSwitchFlippedOn(cell: SLUserSettingTableViewCell, isOn: Bool) {
        for i in 0..<self.settings.count {
            let indexPath = IndexPath(row: i, section: 0)
            if let tableCell:SLUserSettingTableViewCell = self.tableView.cellForRow(at: indexPath)
                as? SLUserSettingTableViewCell , tableCell == cell
            {
                print("cell at \(indexPath.row) is flipped to \(isOn)")
            }
        }
    }
}
