//
//  SLEmergencyContactsViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/10/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import RestService

class SLEmergencyContactsViewController:
SLBaseViewController,
UITableViewDelegate,
UITableViewDataSource,
SLEmergenyContactTableViewCellDelegate
{
    var contacts: [SLEmergencyContact] = []
    
    let maxNumberOfContacts: Int = 3
    
    var onExit:(() -> Void)?
    
    let contactHandler = SLContactHandler()
    
    lazy var tableView:UITableView = {
        let table:UITableView = UITableView(frame: self.view.bounds, style: .grouped)
        table.delegate = self
        table.dataSource = self
        table.rowHeight = 92.0
        table.separatorInset = UIEdgeInsets.zero
        table.backgroundColor = UIColor(white: 242.0/255.0, alpha: 1.0)
        table.register(
            SLEmergenyContactTableViewCell.self,
            forCellReuseIdentifier: String(describing: SLEmergenyContactTableViewCell.self)
        )
        
        return table
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
        
        self.navigationItem.title = NSLocalizedString("EMERGENCY CONTACTS", comment: "")
        let menuImage = UIImage(named: "lock_screen_hamburger_menu")!
        let menuButton:UIBarButtonItem = UIBarButtonItem(
            image: menuImage,
            style: .plain,
            target: self,
            action: #selector(menuButtonPressed)
        )
        
        self.navigationItem.leftBarButtonItem = menuButton
        self.getEmergencyContats()
        
        self.view.addSubview(self.tableView)
    }
    
    func getEmergencyContats() {
        self.contacts.removeAll()
        let emergencyContacts = SLDatabaseManager.shared().emergencyContacts()
        var counter:Int = 0
        for contact in emergencyContacts {
            if counter < self.maxNumberOfContacts {
                self.contacts.append(contact)
                counter += 1
            }
        }
    }
    
    func menuButtonPressed() {
        if self.onExit == nil {
            self.dismiss(animated: true, completion: nil)
        } else {
            self.onExit!()
        }
    }
    
    // MARK: UITableView Delegate and Datasource methods
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return section == 0 ? self.contacts.count : 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            var cell:SLEmergenyContactTableViewCell? = tableView.dequeueReusableCell(
                withIdentifier: String(describing: SLEmergenyContactTableViewCell.self)
                ) as? SLEmergenyContactTableViewCell
            
            if cell == nil {
                cell = SLEmergenyContactTableViewCell(
                    style: .default,
                    reuseIdentifier: String(describing: SLEmergenyContactTableViewCell.self)
                )
            }
            
            var name = "First name Last name"
            if indexPath.row < self.contacts.count {
                let contact = self.contacts[indexPath.row]
                name = contact.fullName()
                if let identifier = contact.contactId {
                    self.contactHandler.getImageForContact(identifier: identifier, completion: { (imageData:NSData?) in
                        DispatchQueue.main.async {
                            if let data = imageData {
                                let image = UIImage(data: data as Data)
                                cell?.updateImage(image: image)
                            } else {
                                cell?.updateImage(image: nil)
                            }
                        }
                    })
                }
            }
            
            
            let image = UIImage(named: "sharing_default_picture")!
            let properties:[SLEmergencyContactTableViewCellProperty:AnyObject] = [
                .Name: name as AnyObject,
                .Pic: image
            ]
            
            cell?.delegate = self
            cell?.setProperties(properties: properties)
            cell?.selectionStyle = .none
            
            return cell!
        }
        
        let cellId:String = "SLEmergenyContactDefaultTableViewCell"
        var cell:UITableViewCell? = tableView.dequeueReusableCell(withIdentifier: cellId)
        if cell == nil {
            cell = UITableViewCell(style: .default, reuseIdentifier: cellId)
        }
        
        cell?.textLabel!.text = NSLocalizedString("My contacts", comment: "")
        cell?.textLabel!.font = UIFont.systemFont(ofSize: 14.0)
        cell?.textLabel!.textColor = UIColor(red: 155, green: 155, blue: 155)
        cell?.imageView!.image = UIImage(named: "contacts_phone_icon")
        cell?.selectionStyle = .none
        
        return cell!
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return section == 0 ? 10.0 : 144.0
    }
    
    func tableView(_ tableView: UITableView, estimatedHeightForHeaderInSection section: Int) -> CGFloat {
        return section == 0 ? 10.0 : 144.0
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.0001
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let viewFrame = CGRect(
            x: 0.0,
            y: 0.0,
            width: tableView.bounds.size.width,
            height: self.tableView(tableView, heightForHeaderInSection: section)
        )
        
        let view = UIView(frame: viewFrame)
        view.backgroundColor = UIColor(white: 242.0/255.0, alpha: 1.0)
        if section == 1 {
            let xPadding:CGFloat = 26.0
            let labelWidth = self.view.bounds.size.width - 2*xPadding
            let utility = SLUtilities()
            let font = UIFont.systemFont(ofSize: 12)
            let text = NSLocalizedString(
                "Nominate 3 close friends or family members as your emergency contacts. In the event of a crash, " +
                "they will be alerted.",
                comment: ""
            )
            let labelSize:CGSize = utility.sizeForLabel(
                font: font,
                text: text,
                maxWidth: labelWidth,
                maxHeight: CGFloat.greatestFiniteMagnitude,
                numberOfLines: 0
            )
            
            let frame = CGRect(
                x: xPadding,
                y: 0.5*(view.bounds.size.height - labelSize.height),
                width: labelWidth,
                height: labelSize.height
            )
            
            let label:UILabel = UILabel(frame: frame)
            label.textColor = UIColor(red: 155, green: 155, blue: 155)
            label.text = text
            label.textAlignment = .center
            label.font = font
            label.numberOfLines = 0
            
            view.addSubview(label)
        }
        
        return view
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.section == 1 {
            let ccvc = ChooseEmengencyContactsViewController(contacts: contacts.flatMap({ EllipseContact(contact: $0) }))
            ccvc.delegate = self
            self.navigationController?.pushViewController(ccvc, animated: true)
        }
    }
    
    
    // MARK: SLEmergencyContactTableViewCellDelegate methods
    func removeButtonPressedOnCell(cell: SLEmergenyContactTableViewCell) {
        for i in 0 ..< self.contacts.count {
            let indexPath = IndexPath(row: i, section: 0)
            let emergencyCell = self.tableView.cellForRow(at: indexPath)
            if cell == emergencyCell {
                let contact = self.contacts[i]
                let dbManager = SLDatabaseManager.shared()
                _ = dbManager.deleteEmergencyContact(contact)
                self.contacts.remove(at: i)
                
                self.tableView.beginUpdates()
                self.tableView.deleteRows(at: [indexPath], with: .left)
                self.tableView.endUpdates()
                
                break
            }
        }
    }
}

extension SLEmergencyContactsViewController: ChooseEmengencyContactsViewControllerDelegate {
    func saveSelected(contacts: [EllipseContact]) {
        let dbMgr = SLDatabaseManager.shared()
        guard let user = dbMgr.getCurrentUser() else {return}
        let dbContacts = dbMgr.emergencyContacts()
        let phoneNumbers = contacts.map({ $0.phoneNumber })
        for contact in dbContacts.filter({ $0.phoneNumber != nil && phoneNumbers.contains($0.phoneNumber!) == false }) {
            dbMgr.deleteEmergencyContact(contact)
        }
        let storeContacts = dbContacts.flatMap({ EllipseContact(contact: $0) })
        for contact in contacts.filter({ storeContacts.contains($0) == false }) {
            let ct = dbMgr.newEmergencyContact()
            ct.fill(contact: contact)
            ct.userId = user.userId
            ct.user = user
            dbMgr.saveEmergencyContact(ct)
        }
        self.contacts = dbMgr.emergencyContacts()
        tableView.reloadData()
    }
}
