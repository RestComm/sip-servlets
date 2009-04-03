class CreateComplaints < ActiveRecord::Migration
  def self.up
    create_table :complaints do |t|
      t.string :customer_name
      t.string :company
      t.text :complaint
      t.string :sip_uri

      t.timestamps
    end
  end

  def self.down
    drop_table :complaints
  end
end
