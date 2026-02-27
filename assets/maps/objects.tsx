<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="objects" tilewidth="41" tileheight="32" tilecount="3" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="player">
  <properties>
   <property name="life" type="int" value="3"/>
   <property name="physics" type="class" propertytype="Physics"/>
  </properties>
  <image source="../graphics/sprites/characters/fox/idle_0.png" width="33" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="9" y="13" width="13" height="17"/>
  </objectgroup>
 </tile>
 <tile id="1" type="gem">
  <properties>
   <property name="animation_speed" type="float" value="0.6"/>
  </properties>
  <image source="../graphics/sprites/sfx/gem/idle_0.png" width="15" height="13"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="2" y="2" width="11" height="9"/>
  </objectgroup>
 </tile>
 <tile id="2" type="enemy">
  <properties>
   <property name="animation_speed" type="float" value="0.8"/>
   <property name="attack" type="class" propertytype="Attack">
    <properties>
     <property name="cooldown" type="float" value="2.5"/>
     <property name="damage" type="int" value="1"/>
     <property name="range" type="float" value="1.5"/>
    </properties>
   </property>
   <property name="life" type="int" value="1"/>
   <property name="physics" type="class" propertytype="Physics">
    <properties>
     <property name="jump_impulse" type="float" value="0"/>
     <property name="max_speed" type="float" value="1.5"/>
    </properties>
   </property>
   <property name="proximity" type="class" propertytype="Proximity">
    <properties>
     <property name="detector_range" type="float" value="7"/>
     <property name="follow_break_range" type="float" value="6"/>
     <property name="follow_range" type="float" value="5"/>
    </properties>
   </property>
  </properties>
  <image source="../graphics/sprites/characters/mushroom/idle_0.png" width="41" height="30"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="10" y="5" width="20" height="24"/>
  </objectgroup>
 </tile>
</tileset>
